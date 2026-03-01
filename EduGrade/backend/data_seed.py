import requests
from datetime import datetime
from src.config.database import get_mongo, get_neo4j, get_redis, get_cassandra

BASE_URL = "http://localhost:5000/api/v1"
HEADERS  = {'Content-Type': 'application/json'}

def log(step, msg):
    print(f"\n[{step}] {msg}")

def post(endpoint, data=None):
    url = f"{BASE_URL}/{endpoint}"
    try:
        response = requests.post(url, json=data or {}, headers=HEADERS)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"❌ Error en {url}: {e}")
        return None

def put(endpoint, data=None):
    url = f"{BASE_URL}/{endpoint}"
    try:
        response = requests.put(url, json=data or {}, headers=HEADERS)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"❌ Error en {url}: {e}")
        return None

def clean_all_datastores():
    print("🧹 Limpiando todas las bases de datos (Mongo, Redis, Neo4j, Cassandra)...")
    try:
        db = get_mongo()
        for c in db.list_collection_names():
            db.drop_collection(c)
    except Exception as e: print(f"   ⚠️ Mongo cleanup falló: {e}")

    try:
        r = get_redis()
        r.flushdb()
    except Exception as e: print(f"   ⚠️ Redis cleanup falló: {e}")

    try:
        with get_neo4j() as session:
            session.run("MATCH (n) DETACH DELETE n")
    except Exception as e: print(f"   ⚠️ Neo4j cleanup falló: {e}")

    try:
        cass = get_cassandra()
        if cass:
            for t in ['registro_auditoria', 'certificados_emitidos', 'entity_metadata', 'historico_reglas']:
                try: cass.execute(f"TRUNCATE {t}")
                except Exception: pass
    except Exception as e: print(f"   ⚠️ Cassandra cleanup falló: {e}")


# ── Helpers de cursada ────────────────────────────────────────────────────────

def inscribir(est_id, mat_id, anio):
    """Crea la relación CURSANDO en Neo4j vía GradingService."""
    post("calificaciones/inscribir", {
        "estudiante_id": est_id,
        "materia_id":    mat_id,
        "anio_lectivo":  str(anio)
    })

def registrar_nota(est_id, mat_id, tipo, nota):
    """
    Guarda la calificación en MongoDB (colección calificaciones) y
    sincroniza la nota en la relación CURSANDO de Neo4j vía GradingService.
    Retorna el id del documento creado.
    """
    res = post("calificaciones/", {
        "estudiante_id": est_id,
        "materia_id":    mat_id,
        "valor_original": {"tipo": tipo, "nota": nota}
    })
    return res.get('id') if res else None

def cerrar(est_id, mat_id):
    """Convierte CURSANDO → CURSÓ con el estado evaluado (APROBADO/REPROBADO) vía GradingService."""
    post("calificaciones/cerrar-cursada", {
        "estudiante_id": est_id,
        "materia_id":    mat_id
    })

def corregir_estado_neo4j(est_id, mat_id, nuevo_estado):
    """
    Corrección directa del estado en la relación CURSÓ de Neo4j para escalas
    de calificación no convencionales donde el servicio no puede inferir
    correctamente si la nota es aprobatoria (ej. escala alemana invertida 1.0-6.0
    o letra "A*" del sistema UK).
    """
    try:
        with get_neo4j() as session:
            session.run("""
                MATCH (e:Estudiante {id_mongo: $eid})-[r:CURSÓ]->(m:Materia {id_mongo: $mid})
                SET r.estado = $estado
            """, eid=est_id, mid=mat_id, estado=nuevo_estado)
    except Exception as e:
        print(f"   ⚠️ Error corrigiendo estado Neo4j: {e}")

def crear_equivalencias(pares):
    """Recibe lista de tuplas (id1, id2) y crea EQUIVALE_A bidireccional en Neo4j."""
    q = "MATCH (a:Materia {id_mongo: $id1}), (b:Materia {id_mongo: $id2}) MERGE (a)-[:EQUIVALE_A]->(b) MERGE (b)-[:EQUIVALE_A]->(a)"
    with get_neo4j() as session:
        for id1, id2 in pares:
            session.run(q, id1=id1, id2=id2)

def agregar_materia_carrera(carrera_id, materia_id):
    post(f"academic/carreras/{carrera_id}/materias/{materia_id}")


def run_seed():
    print("🌱 INICIANDO SEMBRADO DE DATOS (EDUGRADE MULTIMODELO)...")
    clean_all_datastores()

    # ══════════════════════════════════════════
    # 1. INSTITUCIONES
    # ══════════════════════════════════════════
    log("1", "Creando Instituciones (AR, US, UK, DE)...")
    inst_ar = post("academic/instituciones", {"codigo": "UBA",  "nombre": "Universidad de Buenos Aires",        "pais": "AR", "sistema_calificacion": "Numerico 1-10",       "nivel": "Universitario"})['id']
    inst_us = post("academic/instituciones", {"codigo": "MIT",  "nombre": "Mass. Institute of Technology",       "pais": "US", "sistema_calificacion": "Letras A-F / GPA",    "nivel": "Universitario"})['id']
    inst_uk = post("academic/instituciones", {"codigo": "OXF",  "nombre": "Oxford University",                   "pais": "UK", "sistema_calificacion": "Letras A*-F",          "nivel": "Universitario"})['id']
    inst_de = post("academic/instituciones", {"codigo": "TUM",  "nombre": "Technical University of Munich",      "pais": "DE", "sistema_calificacion": "Inverso 1.0-6.0",      "nivel": "Universitario"})['id']
    print(f"   ✅ 4 instituciones creadas.")

    # ══════════════════════════════════════════
    # 2. ADMIN Y PROFESORES
    # ══════════════════════════════════════════
    log("2", "Creando Admin y Profesores...")
    db = get_mongo()
    # El administrador no tiene un endpoint propio; se inserta directamente en MongoDB
    # y se registra su nodo en Neo4j para las relaciones del grafo.
    res_admin = db.administradores.insert_one({
        "legajo": "ADMIN-001", "nombre": "Admin", "apellido": "Global",
        "email": "admin@edugrade.com", "activo": True
    })
    admin_id = str(res_admin.inserted_id)
    with get_neo4j() as session:
        session.run("CREATE (a:Admin {id_mongo: $id, nombre: 'Admin', apellido: 'Global', activo: true})", id=admin_id)
    print("   ✅ Admin creado.")

    prof_ar1 = post("profesores/", {"legajo_docente": "P-AR1", "nombre": "Jorge",    "apellido": "Borges",    "especialidad": "Bases de Datos",        "email": "jorge@mail.com",    "password": "123456", "rol": "profesor"})['id']
    prof_ar2 = post("profesores/", {"legajo_docente": "P-AR2", "nombre": "María",    "apellido": "García",    "especialidad": "Algoritmos",            "email": "maria@mail.com",    "password": "123456", "rol": "profesor"})['id']
    prof_ar3 = post("profesores/", {"legajo_docente": "P-AR3", "nombre": "Carlos",   "apellido": "López",     "especialidad": "Sistemas Operativos",   "email": "carlos@mail.com",   "password": "123456", "rol": "profesor"})['id']
    prof_us1 = post("profesores/", {"legajo_docente": "P-US1", "nombre": "Alan",     "apellido": "Turing",    "especialidad": "Computer Science",      "email": "alan@mail.com",     "password": "123456", "rol": "profesor"})['id']
    prof_us2 = post("profesores/", {"legajo_docente": "P-US2", "nombre": "Lisa",     "apellido": "Chen",      "especialidad": "Machine Learning",      "email": "lisa@mail.com",     "password": "123456", "rol": "profesor"})['id']
    prof_uk1 = post("profesores/", {"legajo_docente": "P-UK1", "nombre": "Ada",      "apellido": "Lovelace",  "especialidad": "Programming",           "email": "ada@mail.com",      "password": "123456", "rol": "profesor"})['id']
    prof_de1 = post("profesores/", {"legajo_docente": "P-DE1", "nombre": "Heinrich", "apellido": "Müller",    "especialidad": "Informatik",            "email": "heinrich@mail.com", "password": "123456", "rol": "profesor"})['id']
    print(f"   ✅ 7 profesores creados.")

    # ══════════════════════════════════════════
    # 3. MATERIAS (4-5 por institución)
    # ══════════════════════════════════════════
    log("3", "Creando Materias por institución...")

    # — UBA (Argentina, escala 1-10) —
    mat_bd_ar    = post("academic/materias", {"codigo": "BD-AR",    "nombre": "Bases de Datos I",            "institucion_id": inst_ar})['id']
    mat_algo_ar  = post("academic/materias", {"codigo": "ALGO-AR",  "nombre": "Algoritmos y Estructuras",    "institucion_id": inst_ar})['id']
    mat_so_ar    = post("academic/materias", {"codigo": "SO-AR",    "nombre": "Sistemas Operativos",         "institucion_id": inst_ar})['id']
    mat_redes_ar = post("academic/materias", {"codigo": "REDES-AR", "nombre": "Redes de Computadoras",       "institucion_id": inst_ar})['id']
    mat_prog_ar  = post("academic/materias", {"codigo": "PROG-AR",  "nombre": "Programación I",              "institucion_id": inst_ar})['id']

    # — MIT (EEUU, escala letras / GPA) —
    mat_bd_us    = post("academic/materias", {"codigo": "CS-300",   "nombre": "Database Systems",            "institucion_id": inst_us})['id']
    mat_algo_us  = post("academic/materias", {"codigo": "CS-401",   "nombre": "Algorithms",                  "institucion_id": inst_us})['id']
    mat_so_us    = post("academic/materias", {"codigo": "CS-502",   "nombre": "Operating Systems",           "institucion_id": inst_us})['id']
    mat_ml_us    = post("academic/materias", {"codigo": "CS-601",   "nombre": "Machine Learning",            "institucion_id": inst_us})['id']
    mat_prog_us  = post("academic/materias", {"codigo": "CS-201",   "nombre": "Programming Fundamentals",    "institucion_id": inst_us})['id']

    # — Oxford (UK, escala A*-F) —
    mat_bd_uk    = post("academic/materias", {"codigo": "MOD-DB",   "nombre": "Data Engineering",            "institucion_id": inst_uk})['id']
    mat_algo_uk  = post("academic/materias", {"codigo": "MOD-ALGO", "nombre": "Computational Thinking",      "institucion_id": inst_uk})['id']
    mat_so_uk    = post("academic/materias", {"codigo": "MOD-OS",   "nombre": "Advanced Operating Systems",  "institucion_id": inst_uk})['id']
    mat_ml_uk    = post("academic/materias", {"codigo": "MOD-ML",   "nombre": "Machine Learning & AI",       "institucion_id": inst_uk})['id']

    # — TUM (Alemania, escala invertida 1.0-6.0) —
    mat_bd_de    = post("academic/materias", {"codigo": "INF-DB",   "nombre": "Datenbanksysteme",             "institucion_id": inst_de})['id']
    mat_algo_de  = post("academic/materias", {"codigo": "INF-ALGO", "nombre": "Algorithmen",                  "institucion_id": inst_de})['id']
    mat_so_de    = post("academic/materias", {"codigo": "INF-OS",   "nombre": "Betriebssysteme",              "institucion_id": inst_de})['id']
    mat_redes_de = post("academic/materias", {"codigo": "INF-NET",  "nombre": "Netzwerke und Kommunikation",  "institucion_id": inst_de})['id']

    print(f"   ✅ 18 materias creadas en 4 instituciones.")

    # Asignación de materias a profesores
    post(f"profesores/{prof_ar1}/asignar-materia", {"materia_id": mat_bd_ar})
    post(f"profesores/{prof_ar2}/asignar-materia", {"materia_id": mat_algo_ar})
    post(f"profesores/{prof_ar3}/asignar-materia", {"materia_id": mat_so_ar})
    post(f"profesores/{prof_ar3}/asignar-materia", {"materia_id": mat_redes_ar})
    post(f"profesores/{prof_us1}/asignar-materia", {"materia_id": mat_bd_us})
    post(f"profesores/{prof_us1}/asignar-materia", {"materia_id": mat_algo_us})
    post(f"profesores/{prof_us2}/asignar-materia", {"materia_id": mat_ml_us})
    post(f"profesores/{prof_uk1}/asignar-materia", {"materia_id": mat_bd_uk})
    post(f"profesores/{prof_de1}/asignar-materia", {"materia_id": mat_bd_de})
    post(f"profesores/{prof_de1}/asignar-materia", {"materia_id": mat_algo_de})

    # Equivalencias entre instituciones (misma disciplina, diferente escala)
    crear_equivalencias([
        (mat_bd_ar,   mat_bd_us),   (mat_bd_us,   mat_bd_uk),   (mat_bd_uk,   mat_bd_de),
        (mat_algo_ar, mat_algo_us), (mat_algo_us, mat_algo_uk), (mat_algo_uk, mat_algo_de),
        (mat_so_ar,   mat_so_us),   (mat_so_us,   mat_so_uk),   (mat_so_uk,   mat_so_de),
        (mat_redes_ar, mat_redes_de),
    ])
    print("   ✅ Equivalencias entre instituciones creadas.")

    # ══════════════════════════════════════════
    # 4. CARRERAS
    # ══════════════════════════════════════════
    log("4", "Creando Carreras y asignando materias...")

    car_sistemas = post("academic/carreras", {"codigo": "IS-UBA",  "nombre": "Ingeniería en Sistemas"})['id']
    car_cs_us    = post("academic/carreras", {"codigo": "CS-MIT",  "nombre": "Computer Science"})['id']
    car_cs_uk    = post("academic/carreras", {"codigo": "CS-OXF",  "nombre": "Computer Science Oxford"})['id']
    car_inf_de   = post("academic/carreras", {"codigo": "INF-TUM", "nombre": "Informatik"})['id']

    for mat in [mat_bd_ar, mat_algo_ar, mat_so_ar, mat_redes_ar, mat_prog_ar]:
        agregar_materia_carrera(car_sistemas, mat)

    for mat in [mat_bd_us, mat_algo_us, mat_so_us, mat_ml_us, mat_prog_us]:
        agregar_materia_carrera(car_cs_us, mat)

    for mat in [mat_bd_uk, mat_algo_uk, mat_so_uk, mat_ml_uk]:
        agregar_materia_carrera(car_cs_uk, mat)

    for mat in [mat_bd_de, mat_algo_de, mat_so_de, mat_redes_de]:
        agregar_materia_carrera(car_inf_de, mat)

    print("   ✅ 4 carreras creadas con sus materias asignadas.")

    # ══════════════════════════════════════════
    # 4b. REGLAS DE CONVERSIÓN
    # Se usan los servicios para que también queden cacheadas en Redis (TTL 7 días).
    # ══════════════════════════════════════════
    log("4b", "Creando Reglas de Conversión via service (MongoDB + Redis)...")

    reglas_conversion = [
        {"codigo_regla": "AR_TO_UK",     "nombre": "Argentina → Reino Unido (GCSE/A-Level)", "mapeo": [
            {"nota_origen": 10, "nota_destino": "A*"}, {"nota_origen": 9, "nota_destino": "A"},
            {"nota_origen": 8,  "nota_destino": "B"},  {"nota_origen": 7, "nota_destino": "C"},
            {"nota_origen": 6,  "nota_destino": "D"},  {"nota_origen": 5, "nota_destino": "E"},
            {"nota_origen": 4,  "nota_destino": "F"},  {"nota_origen": 3, "nota_destino": "F"},
            {"nota_origen": 2,  "nota_destino": "F"},  {"nota_origen": 1, "nota_destino": "F"},
        ]},
        {"codigo_regla": "AR_TO_US",     "nombre": "Argentina → Estados Unidos (Letras)", "mapeo": [
            {"nota_origen": 10, "nota_destino": "A"}, {"nota_origen": 9, "nota_destino": "A"},
            {"nota_origen": 8,  "nota_destino": "B"}, {"nota_origen": 7, "nota_destino": "C"},
            {"nota_origen": 6,  "nota_destino": "D"}, {"nota_origen": 5, "nota_destino": "F"},
            {"nota_origen": 4,  "nota_destino": "F"}, {"nota_origen": 3, "nota_destino": "F"},
            {"nota_origen": 2,  "nota_destino": "F"}, {"nota_origen": 1, "nota_destino": "F"},
        ]},
        {"codigo_regla": "AR_TO_US_GPA", "nombre": "Argentina → Estados Unidos (GPA 0-4)", "mapeo": [
            {"nota_origen": 10, "nota_destino": 4.0}, {"nota_origen": 9, "nota_destino": 3.5},
            {"nota_origen": 8,  "nota_destino": 3.0}, {"nota_origen": 7, "nota_destino": 2.5},
            {"nota_origen": 6,  "nota_destino": 2.0}, {"nota_origen": 5, "nota_destino": 1.0},
            {"nota_origen": 4,  "nota_destino": 0.0}, {"nota_origen": 3, "nota_destino": 0.0},
            {"nota_origen": 2,  "nota_destino": 0.0}, {"nota_origen": 1, "nota_destino": 0.0},
        ]},
        {"codigo_regla": "AR_TO_DE",     "nombre": "Argentina → Alemania (1.0-6.0)", "mapeo": [
            {"nota_origen": 10, "nota_destino": 1.0}, {"nota_origen": 9, "nota_destino": 1.3},
            {"nota_origen": 8,  "nota_destino": 2.0}, {"nota_origen": 7, "nota_destino": 2.5},
            {"nota_origen": 6,  "nota_destino": 4.0}, {"nota_origen": 5, "nota_destino": 5.0},
            {"nota_origen": 4,  "nota_destino": 6.0}, {"nota_origen": 3, "nota_destino": 6.0},
            {"nota_origen": 2,  "nota_destino": 6.0}, {"nota_origen": 1, "nota_destino": 6.0},
        ]},
        {"codigo_regla": "UK_TO_AR",     "nombre": "Reino Unido → Argentina", "mapeo": [
            {"nota_origen": "A*", "nota_destino": 10}, {"nota_origen": "A", "nota_destino": 9},
            {"nota_origen": "B",  "nota_destino": 8},  {"nota_origen": "C", "nota_destino": 7},
            {"nota_origen": "D",  "nota_destino": 6},  {"nota_origen": "E", "nota_destino": 5},
            {"nota_origen": "F",  "nota_destino": 4},
        ]},
        {"codigo_regla": "UK_TO_US",     "nombre": "Reino Unido → Estados Unidos", "mapeo": [
            {"nota_origen": "A*", "nota_destino": "A"}, {"nota_origen": "A", "nota_destino": "A"},
            {"nota_origen": "B",  "nota_destino": "B"}, {"nota_origen": "C", "nota_destino": "C"},
            {"nota_origen": "D",  "nota_destino": "D"}, {"nota_origen": "E", "nota_destino": "F"},
            {"nota_origen": "F",  "nota_destino": "F"},
        ]},
        {"codigo_regla": "UK_TO_DE",     "nombre": "Reino Unido → Alemania", "mapeo": [
            {"nota_origen": "A*", "nota_destino": 1.0}, {"nota_origen": "A", "nota_destino": 1.3},
            {"nota_origen": "B",  "nota_destino": 2.0}, {"nota_origen": "C", "nota_destino": 2.5},
            {"nota_origen": "D",  "nota_destino": 4.0}, {"nota_origen": "E", "nota_destino": 5.0},
            {"nota_origen": "F",  "nota_destino": 6.0},
        ]},
        {"codigo_regla": "US_TO_AR",     "nombre": "Estados Unidos (Letras) → Argentina", "mapeo": [
            {"nota_origen": "A",  "nota_destino": 10}, {"nota_origen": "A+", "nota_destino": 10}, {"nota_origen": "A-", "nota_destino": 9},
            {"nota_origen": "B",  "nota_destino": 8},  {"nota_origen": "B+", "nota_destino": 8},  {"nota_origen": "B-", "nota_destino": 7},
            {"nota_origen": "C",  "nota_destino": 7},  {"nota_origen": "C+", "nota_destino": 6},  {"nota_origen": "C-", "nota_destino": 6},
            {"nota_origen": "D",  "nota_destino": 6},  {"nota_origen": "F",  "nota_destino": 4},
        ]},
        {"codigo_regla": "US_TO_UK",     "nombre": "Estados Unidos → Reino Unido", "mapeo": [
            {"nota_origen": "A",  "nota_destino": "A"},  {"nota_origen": "A+", "nota_destino": "A*"}, {"nota_origen": "A-", "nota_destino": "A"},
            {"nota_origen": "B",  "nota_destino": "B"},  {"nota_origen": "B+", "nota_destino": "B"},  {"nota_origen": "B-", "nota_destino": "B"},
            {"nota_origen": "C",  "nota_destino": "C"},  {"nota_origen": "C+", "nota_destino": "C"},  {"nota_origen": "C-", "nota_destino": "C"},
            {"nota_origen": "D",  "nota_destino": "D"},  {"nota_origen": "F",  "nota_destino": "F"},
        ]},
        {"codigo_regla": "US_TO_DE",     "nombre": "Estados Unidos → Alemania", "mapeo": [
            {"nota_origen": "A",  "nota_destino": 1.0}, {"nota_origen": "A+", "nota_destino": 1.0}, {"nota_origen": "A-", "nota_destino": 1.3},
            {"nota_origen": "B",  "nota_destino": 2.0}, {"nota_origen": "B+", "nota_destino": 1.7}, {"nota_origen": "B-", "nota_destino": 2.3},
            {"nota_origen": "C",  "nota_destino": 3.0}, {"nota_origen": "C+", "nota_destino": 2.5}, {"nota_origen": "C-", "nota_destino": 3.3},
            {"nota_origen": "D",  "nota_destino": 4.0}, {"nota_origen": "F",  "nota_destino": 6.0},
        ]},
        {"codigo_regla": "US_GPA_TO_AR", "nombre": "Estados Unidos (GPA) → Argentina", "mapeo": [
            {"nota_origen": 4.0, "nota_destino": 10}, {"nota_origen": 3.5, "nota_destino": 9},
            {"nota_origen": 3.0, "nota_destino": 8},  {"nota_origen": 2.5, "nota_destino": 7},
            {"nota_origen": 2.0, "nota_destino": 6},  {"nota_origen": 1.5, "nota_destino": 5},
            {"nota_origen": 1.0, "nota_destino": 4},  {"nota_origen": 0.5, "nota_destino": 4},
            {"nota_origen": 0.0, "nota_destino": 4},
        ]},
        {"codigo_regla": "DE_TO_AR",     "nombre": "Alemania → Argentina", "mapeo": [
            {"nota_origen": 1.0, "nota_destino": 10}, {"nota_origen": 1.3, "nota_destino": 9}, {"nota_origen": 1.7, "nota_destino": 9},
            {"nota_origen": 2.0, "nota_destino": 8},  {"nota_origen": 2.3, "nota_destino": 8}, {"nota_origen": 2.7, "nota_destino": 7},
            {"nota_origen": 3.0, "nota_destino": 7},  {"nota_origen": 3.3, "nota_destino": 7}, {"nota_origen": 3.7, "nota_destino": 6},
            {"nota_origen": 4.0, "nota_destino": 6},  {"nota_origen": 4.3, "nota_destino": 5}, {"nota_origen": 4.7, "nota_destino": 5},
            {"nota_origen": 5.0, "nota_destino": 4},  {"nota_origen": 5.3, "nota_destino": 4}, {"nota_origen": 5.7, "nota_destino": 4},
            {"nota_origen": 6.0, "nota_destino": 4},
        ]},
        {"codigo_regla": "DE_TO_UK",     "nombre": "Alemania → Reino Unido", "mapeo": [
            {"nota_origen": 1.0, "nota_destino": "A*"}, {"nota_origen": 1.3, "nota_destino": "A"}, {"nota_origen": 1.7, "nota_destino": "A"},
            {"nota_origen": 2.0, "nota_destino": "B"},  {"nota_origen": 2.3, "nota_destino": "B"}, {"nota_origen": 2.7, "nota_destino": "C"},
            {"nota_origen": 3.0, "nota_destino": "C"},  {"nota_origen": 3.3, "nota_destino": "C"}, {"nota_origen": 3.7, "nota_destino": "D"},
            {"nota_origen": 4.0, "nota_destino": "D"},  {"nota_origen": 4.3, "nota_destino": "E"}, {"nota_origen": 4.7, "nota_destino": "E"},
            {"nota_origen": 5.0, "nota_destino": "F"},  {"nota_origen": 5.3, "nota_destino": "F"}, {"nota_origen": 5.7, "nota_destino": "F"},
            {"nota_origen": 6.0, "nota_destino": "F"},
        ]},
        {"codigo_regla": "DE_TO_US",     "nombre": "Alemania → Estados Unidos", "mapeo": [
            {"nota_origen": 1.0, "nota_destino": "A"}, {"nota_origen": 1.3, "nota_destino": "A"}, {"nota_origen": 1.7, "nota_destino": "A"},
            {"nota_origen": 2.0, "nota_destino": "B"}, {"nota_origen": 2.3, "nota_destino": "B"}, {"nota_origen": 2.7, "nota_destino": "C"},
            {"nota_origen": 3.0, "nota_destino": "C"}, {"nota_origen": 3.3, "nota_destino": "C"}, {"nota_origen": 3.7, "nota_destino": "D"},
            {"nota_origen": 4.0, "nota_destino": "D"}, {"nota_origen": 4.3, "nota_destino": "F"}, {"nota_origen": 4.7, "nota_destino": "F"},
            {"nota_origen": 5.0, "nota_destino": "F"}, {"nota_origen": 5.3, "nota_destino": "F"}, {"nota_origen": 5.7, "nota_destino": "F"},
            {"nota_origen": 6.0, "nota_destino": "F"},
        ]},
    ]

    for regla in reglas_conversion:
        post("calificaciones/reglas", regla)
    print(f"   ✅ {len(reglas_conversion)} reglas de conversión creadas y cacheadas en Redis.")

    # ══════════════════════════════════════════
    # 5. ESTUDIANTES
    # ══════════════════════════════════════════
    log("5", "Creando Estudiantes y vinculando a Instituciones...")

    # Argentina (UBA)
    est_fede  = post("estudiantes/", {"legajo": "L-111", "nombre": "Federico", "apellido": "Recursante", "email": "fede@mail.com",   "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_fede}",  {"institucion_id": inst_ar})

    est_ana   = post("estudiantes/", {"legajo": "L-222", "nombre": "Ana",      "apellido": "Aprevio",    "email": "ana@mail.com",    "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_ana}",   {"institucion_id": inst_ar})

    est_lucas = post("estudiantes/", {"legajo": "L-999", "nombre": "Lucas",    "apellido": "Activo",     "email": "lucas@mail.com",  "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_lucas}", {"institucion_id": inst_ar})

    est_maria = post("estudiantes/", {"legajo": "L-444", "nombre": "María",    "apellido": "González",   "email": "mariag@mail.com", "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_maria}", {"institucion_id": inst_ar})

    est_pablo = post("estudiantes/", {"legajo": "L-555", "nombre": "Pablo",    "apellido": "Rodríguez",  "email": "pablo@mail.com",  "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_pablo}", {"institucion_id": inst_ar})

    # EEUU (MIT)
    est_john  = post("estudiantes/", {"legajo": "L-333", "nombre": "John",     "apellido": "Exchange",   "email": "john@mail.com",   "pais": "US", "password": "123456"})['id']
    put(f"estudiantes/{est_john}",  {"institucion_id": inst_us})

    est_emma  = post("estudiantes/", {"legajo": "L-888", "nombre": "Emma",     "apellido": "Current",    "email": "emma@mail.com",   "pais": "US", "password": "123456"})['id']
    put(f"estudiantes/{est_emma}",  {"institucion_id": inst_us})

    est_mike  = post("estudiantes/", {"legajo": "L-777", "nombre": "Michael",  "apellido": "Carter",     "email": "mike@mail.com",   "pais": "US", "password": "123456"})['id']
    put(f"estudiantes/{est_mike}",  {"institucion_id": inst_us})

    # UK (Oxford)
    est_sophie = post("estudiantes/", {"legajo": "L-600", "nombre": "Sophie",  "apellido": "Williams",   "email": "sophie@mail.com", "pais": "UK", "password": "123456"})['id']
    put(f"estudiantes/{est_sophie}", {"institucion_id": inst_uk})

    # Alemania (TUM)
    est_klaus = post("estudiantes/", {"legajo": "L-700", "nombre": "Klaus",    "apellido": "Becker",     "email": "klaus@mail.com",  "pais": "DE", "password": "123456"})['id']
    put(f"estudiantes/{est_klaus}", {"institucion_id": inst_de})

    # Argentina (UBA) — estudiante demo para el escenario "profesor carga nota"
    est_vale  = post("estudiantes/", {"legajo": "L-DM1", "nombre": "Valentina", "apellido": "Fernández",  "email": "vale@mail.com",   "pais": "AR", "password": "123456"})['id']
    put(f"estudiantes/{est_vale}",  {"institucion_id": inst_ar})

    print(f"   ✅ 11 estudiantes creados y vinculados.")

    # ══════════════════════════════════════════
    # 6. CURSADAS Y CALIFICACIONES
    # Flujo completo por service para cada cursada:
    #   inscribir() → registrar_nota() × N → cerrar()
    # Las calificaciones se persisten en MongoDB (colección 'calificaciones')
    # y se sincronizan en el grafo Neo4j (relaciones CURSANDO → CURSÓ).
    # Para cursadas activas no se llama a cerrar().
    # Para escalas no convencionales (UK A*, Alemania 1.0-6.0) se corrige
    # el estado en Neo4j con corregir_estado_neo4j() ya que el servicio
    # normaliza las notas como valor numérico para evaluar aprobación.
    # ══════════════════════════════════════════
    log("6", "Simulando Cursadas y Calificaciones via GradingService...")

    # ── Federico: recursó BD (2024) y volvió a reprobar en 2025; aprobó Algo y Prog ──
    print("   👉 Federico: recursa BD (2024 → REPROBADO), vuelve a reprobar en 2025; Algo y Prog aprobadas")
    # BD 2024 — reprobó (nota final = 2)
    inscribir(est_fede, mat_bd_ar, "2024")
    registrar_nota(est_fede, mat_bd_ar, "PARCIAL_1", 2)
    registrar_nota(est_fede, mat_bd_ar, "FINAL",     2)
    cerrar(est_fede, mat_bd_ar)                          # REPROBADO (2 < 4)
    # BD 2025 — recursó y volvió a reprobar (nota final = 3)
    inscribir(est_fede, mat_bd_ar, "2025")
    registrar_nota(est_fede, mat_bd_ar, "PARCIAL_1", 3)
    registrar_nota(est_fede, mat_bd_ar, "FINAL",     3)
    cerrar(est_fede, mat_bd_ar)                          # REPROBADO (3 < 4) → debe recursar
    # Algoritmos 2025 — aprobó
    inscribir(est_fede, mat_algo_ar, "2025")
    registrar_nota(est_fede, mat_algo_ar, "PARCIAL_1", 6)
    registrar_nota(est_fede, mat_algo_ar, "FINAL",     7)
    cerrar(est_fede, mat_algo_ar)                        # APROBADO
    # Programación 2024 — aprobó
    inscribir(est_fede, mat_prog_ar, "2024")
    registrar_nota(est_fede, mat_prog_ar, "PARCIAL_1", 9)
    registrar_nota(est_fede, mat_prog_ar, "FINAL",     9)
    cerrar(est_fede, mat_prog_ar)                        # APROBADO

    # ── Ana: fue a previo en BD y aprobó; Prog aprobada; SO cursada activa ──
    print("   👉 Ana: previo en BD (final 3 → previo 7 → APROBADO), SO cursada activa")
    # BD 2025 — fue a previo (final < 4, previo >= 4 → APROBADO al cerrar)
    inscribir(est_ana, mat_bd_ar, "2025")
    registrar_nota(est_ana, mat_bd_ar, "PARCIAL_1", 6)
    registrar_nota(est_ana, mat_bd_ar, "FINAL",     3)   # final < 4: necesita previo
    registrar_nota(est_ana, mat_bd_ar, "PREVIO",    7)   # previo >= 4
    cerrar(est_ana, mat_bd_ar)                           # APROBADO (previo=7 >= 4)
    # Programación 2024 — aprobó
    inscribir(est_ana, mat_prog_ar, "2024")
    registrar_nota(est_ana, mat_prog_ar, "PARCIAL_1", 8)
    registrar_nota(est_ana, mat_prog_ar, "FINAL",     8)
    cerrar(est_ana, mat_prog_ar)                         # APROBADO
    # Sistemas Operativos 2026 — cursada activa
    inscribir(est_ana, mat_so_ar, "2026")
    registrar_nota(est_ana, mat_so_ar, "PARCIAL_1", 7)

    # ── Lucas: cursada activa en BD ──
    print("   👉 Lucas: cursada activa en BD")
    inscribir(est_lucas, mat_bd_ar, "2026")
    registrar_nota(est_lucas, mat_bd_ar, "PARCIAL_1", 8)

    # ── María: alumna regular avanzada ──
    print("   👉 María: alumna regular avanzada (Prog, Algo, BD aprobadas; Redes activa)")
    # Programación 2023 — aprobó
    inscribir(est_maria, mat_prog_ar, "2023")
    registrar_nota(est_maria, mat_prog_ar, "PARCIAL_1", 10)
    registrar_nota(est_maria, mat_prog_ar, "FINAL",     10)
    cerrar(est_maria, mat_prog_ar)                       # APROBADO
    # Algoritmos 2024 — aprobó
    inscribir(est_maria, mat_algo_ar, "2024")
    registrar_nota(est_maria, mat_algo_ar, "PARCIAL_1", 9)
    registrar_nota(est_maria, mat_algo_ar, "FINAL",     9)
    cerrar(est_maria, mat_algo_ar)                       # APROBADO
    # BD 2025 — aprobó
    inscribir(est_maria, mat_bd_ar, "2025")
    registrar_nota(est_maria, mat_bd_ar, "PARCIAL_1", 8)
    registrar_nota(est_maria, mat_bd_ar, "FINAL",     9)
    cerrar(est_maria, mat_bd_ar)                         # APROBADO
    # Redes 2026 — cursada activa
    inscribir(est_maria, mat_redes_ar, "2026")
    registrar_nota(est_maria, mat_redes_ar, "PARCIAL_1", 7)

    # ── Pablo: fue a previo en Prog; reprobó BD; Algo cursada activa ──
    print("   👉 Pablo: previo en Prog (aprueba), reprueba BD, Algo activa")
    # Programación 2024 — fue a previo y aprobó
    inscribir(est_pablo, mat_prog_ar, "2024")
    registrar_nota(est_pablo, mat_prog_ar, "PARCIAL_1", 5)
    registrar_nota(est_pablo, mat_prog_ar, "FINAL",     3)   # final < 4: necesita previo
    registrar_nota(est_pablo, mat_prog_ar, "PREVIO",    6)   # previo >= 4
    cerrar(est_pablo, mat_prog_ar)                           # APROBADO (previo=6 >= 4)
    # BD 2025 — reprobó
    inscribir(est_pablo, mat_bd_ar, "2025")
    registrar_nota(est_pablo, mat_bd_ar, "PARCIAL_1", 4)
    registrar_nota(est_pablo, mat_bd_ar, "FINAL",     3)
    cerrar(est_pablo, mat_bd_ar)                             # REPROBADO
    # Algoritmos 2026 — cursada activa
    inscribir(est_pablo, mat_algo_ar, "2026")
    registrar_nota(est_pablo, mat_algo_ar, "PARCIAL_1", 6)

    # ── John (EEUU → UBA): aprobó en MIT, luego aprobó en UBA ──
    print("   👉 John: aprueba en MIT (DB y Algo) y rinde BD en UBA")
    # Database Systems MIT 2024 — "A" → normaliza a 10
    inscribir(est_john, mat_bd_us, "2024")
    registrar_nota(est_john, mat_bd_us, "FINAL", "A")
    cerrar(est_john, mat_bd_us)                              # APROBADO (A → 10 >= 4)
    # Algorithms MIT 2024 — "B" → normaliza a 7
    inscribir(est_john, mat_algo_us, "2024")
    registrar_nota(est_john, mat_algo_us, "FINAL", "B")
    cerrar(est_john, mat_algo_us)                            # APROBADO (B → 7 >= 4)
    # BD UBA 2025 — aprobó directamente
    inscribir(est_john, mat_bd_ar, "2025")
    registrar_nota(est_john, mat_bd_ar, "FINAL", 9)
    cerrar(est_john, mat_bd_ar)                              # APROBADO

    # ── Emma: cursadas activas en MIT ──
    print("   👉 Emma: cursadas activas en MIT (BD y Algo)")
    inscribir(est_emma, mat_bd_us,   "2026")
    registrar_nota(est_emma, mat_bd_us,   "PARCIAL_1", "B")  # B → 7
    inscribir(est_emma, mat_algo_us, "2026")
    registrar_nota(est_emma, mat_algo_us, "PARCIAL_1", "A")  # A → 10

    # ── Michael: alumno avanzado MIT ──
    print("   👉 Michael: alumno avanzado MIT (Prog, DB, Algo aprobadas; SO activa)")
    # Programming Fundamentals 2022
    inscribir(est_mike, mat_prog_us, "2022")
    registrar_nota(est_mike, mat_prog_us, "FINAL", "A")
    cerrar(est_mike, mat_prog_us)                            # APROBADO
    # Database Systems 2023
    inscribir(est_mike, mat_bd_us, "2023")
    registrar_nota(est_mike, mat_bd_us, "FINAL", "A")
    cerrar(est_mike, mat_bd_us)                              # APROBADO
    # Algorithms 2024
    inscribir(est_mike, mat_algo_us, "2024")
    registrar_nota(est_mike, mat_algo_us, "FINAL", "B")
    cerrar(est_mike, mat_algo_us)                            # APROBADO
    # Operating Systems 2026 — cursada activa
    inscribir(est_mike, mat_so_us, "2026")
    registrar_nota(est_mike, mat_so_us, "PARCIAL_1", "A")

    # ── Sophie (Oxford): escala A*-F ──
    # Nota "A*" no está en el mapa de normalización del servicio (A→10, B→7, etc.),
    # por lo que se guarda correctamente en MongoDB pero Neo4j recibe 0 como valor.
    # Se corrige el estado CURSÓ manualmente tras el cierre.
    print("   👉 Sophie: alumna Oxford (BD con A*, Algo con A aprobadas; SO activa)")
    # Data Engineering 2024 — "A*" (excelente en escala UK)
    inscribir(est_sophie, mat_bd_uk, "2024")
    registrar_nota(est_sophie, mat_bd_uk, "FINAL", "A*")     # MongoDB: A*; Neo4j: 0 (no normalizable)
    cerrar(est_sophie, mat_bd_uk)                            # cerrar da REPROBADO → corregir
    corregir_estado_neo4j(est_sophie, mat_bd_uk, "APROBADO") # A* = excelente en sistema UK
    # Computational Thinking 2024 — "A"
    inscribir(est_sophie, mat_algo_uk, "2024")
    registrar_nota(est_sophie, mat_algo_uk, "FINAL", "A")
    cerrar(est_sophie, mat_algo_uk)                          # APROBADO (A → 10 >= 4)
    # Advanced Operating Systems 2026 — cursada activa
    inscribir(est_sophie, mat_so_uk, "2026")
    registrar_nota(est_sophie, mat_so_uk, "PARCIAL_1", "B")

    # ── Klaus (TUM): escala alemana invertida 1.0-6.0 (1.0=mejor, 4.0=mínimo aprobatorio) ──
    # El servicio normaliza todos los valores como numéricos; al evaluar cerrar_cursada
    # compara nota >= 4 para aprobar. En la escala alemana 1.3 y 2.0 son aprobatorias
    # pero quedan por debajo de 4, por lo que se corrige el estado tras el cierre.
    print("   👉 Klaus: alumno TUM escala alemana (BD 1.3, Algo 2.0 aprobadas; Redes activa)")
    # Datenbanksysteme 2023 — nota 1.3 (excelente en escala alemana)
    inscribir(est_klaus, mat_bd_de, "2023")
    registrar_nota(est_klaus, mat_bd_de, "FINAL", 1.3)       # MongoDB: 1.3; Neo4j: 1.3 (< 4)
    cerrar(est_klaus, mat_bd_de)                             # cerrar da REPROBADO → corregir
    corregir_estado_neo4j(est_klaus, mat_bd_de, "APROBADO")  # 1.3 = Sehr gut (muy bueno)
    # Algorithmen 2024 — nota 2.0 (bueno)
    inscribir(est_klaus, mat_algo_de, "2024")
    registrar_nota(est_klaus, mat_algo_de, "FINAL", 2.0)
    cerrar(est_klaus, mat_algo_de)                           # cerrar da REPROBADO → corregir
    corregir_estado_neo4j(est_klaus, mat_algo_de, "APROBADO")
    # Netzwerke 2026 — cursada activa
    inscribir(est_klaus, mat_redes_de, "2026")
    registrar_nota(est_klaus, mat_redes_de, "PARCIAL_1", 1.7)

    # ── Valentina (UBA): estudiante demo — historial cargado + BD activa para demo profesor ──
    # Tiene materias aprobadas, una reprobada y una cursada activa en BD-AR 2026.
    # Escenario demo: jorge@mail.com ingresa como profesor, ve a Bases de Datos I,
    # y carga el 2° parcial y/o final de Valentina. Valentina luego inicia sesión
    # como vale@mail.com y ve las notas actualizadas en su perfil.
    print("   👉 Valentina: demo profesor-nota (Prog y Algo aprobadas; Redes reprobada; BD activa 2026)")
    # Programación 2023 — aprobó
    inscribir(est_vale, mat_prog_ar, "2023")
    registrar_nota(est_vale, mat_prog_ar, "PARCIAL_1", 8)
    registrar_nota(est_vale, mat_prog_ar, "FINAL",     8)
    cerrar(est_vale, mat_prog_ar)                            # APROBADO
    # Algoritmos 2024 — aprobó
    inscribir(est_vale, mat_algo_ar, "2024")
    registrar_nota(est_vale, mat_algo_ar, "PARCIAL_1", 7)
    registrar_nota(est_vale, mat_algo_ar, "FINAL",     7)
    cerrar(est_vale, mat_algo_ar)                            # APROBADO
    # Redes 2025 — reprobó
    inscribir(est_vale, mat_redes_ar, "2025")
    registrar_nota(est_vale, mat_redes_ar, "PARCIAL_1", 3)
    registrar_nota(est_vale, mat_redes_ar, "FINAL",     2)
    cerrar(est_vale, mat_redes_ar)                           # REPROBADO (2 < 4)
    # Bases de Datos I 2026 — CURSADA ACTIVA (solo parcial_1 cargado)
    # Jorge (jorge@mail.com) puede agregar parcial_2 y/o final desde su dashboard
    inscribir(est_vale, mat_bd_ar, "2026")
    registrar_nota(est_vale, mat_bd_ar, "PARCIAL_1", 5)

    n_calificaciones = len(list(get_mongo().calificaciones.find()))
    print(f"\n✅ DATA SEED FINALIZADO EXITOSAMENTE.")
    print(f"   Instituciones : 4  |  Profesores : 7")
    print(f"   Materias       : 18 |  Carreras   : 4")
    print(f"   Estudiantes    : 11 |  Reglas CV  : {len(reglas_conversion)}")
    print(f"   Calificaciones : {n_calificaciones} documentos en MongoDB")
    print(f"\n🎓 ESCENARIO DEMO (cargar nota profesor → perfil estudiante):")
    print(f"   1. Iniciar sesión como profesor: jorge@mail.com / 123456")
    print(f"   2. Seleccionar materia: Bases de Datos I (BD-AR)")
    print(f"   3. Buscar a Valentina Fernández (L-DM1) — tiene Parcial 1: 5")
    print(f"   4. Cargar 2° Parcial y/o Final y guardar")
    print(f"   5. Iniciar sesión como estudiante: vale@mail.com / 123456")
    print(f"   6. Ver las notas actualizadas en el perfil")


if __name__ == "__main__":
    run_seed()
