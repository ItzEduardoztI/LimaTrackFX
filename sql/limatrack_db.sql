-- =====================================================================
--  LimaTrack — Script de Base de Datos
--  Motor: MySQL 8.0 (MySQL Workbench)
--  Descripción: Sistema de monitoreo de flota de buses de transporte
--               público en Lima, Perú.
-- =====================================================================

DROP DATABASE IF EXISTS limatrack_db;
CREATE DATABASE limatrack_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE limatrack_db;

-- =====================================================================
-- 1. EMPRESAS DE TRANSPORTE
-- =====================================================================
CREATE TABLE empresas (
    id_empresa      INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    ruc             VARCHAR(11)  NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    direccion       VARCHAR(150),
    fecha_registro  DATE NOT NULL DEFAULT (CURRENT_DATE)
) ENGINE=InnoDB;

-- =====================================================================
-- 2. USUARIOS (Operadores, Conductores, Administradores)
-- =====================================================================
CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(100) NOT NULL,
    correo          VARCHAR(100) NOT NULL UNIQUE,
    contrasena      VARCHAR(100) NOT NULL,   -- demo: texto plano. En producción usar hash (BCrypt).
    rol             ENUM('OPERADOR','CONDUCTOR','ADMIN') NOT NULL DEFAULT 'OPERADOR',
    iniciales       VARCHAR(3) NOT NULL,
    id_empresa      INT NOT NULL,
    activo          TINYINT(1) NOT NULL DEFAULT 1,
    licencia_conducir VARCHAR(20) NULL,
    fecha_creacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB;

-- =====================================================================
-- 3. RUTAS
-- =====================================================================
CREATE TABLE rutas (
    id_ruta         INT AUTO_INCREMENT PRIMARY KEY,
    codigo          VARCHAR(10) NOT NULL UNIQUE,
    nombre          VARCHAR(100) NOT NULL,
    origen          VARCHAR(60) NOT NULL,
    destino         VARCHAR(60) NOT NULL,
    distancia_km    DECIMAL(5,2) NOT NULL,
    tiempo_estimado_min INT NOT NULL,
    tarifa          DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    id_empresa      INT NOT NULL,
    activa          TINYINT(1) NOT NULL DEFAULT 1,
    CONSTRAINT fk_ruta_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB;

-- =====================================================================
-- 4. PARADEROS
-- =====================================================================
CREATE TABLE paraderos (
    id_paradero     INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    distrito        VARCHAR(60) NOT NULL,
    latitud         DECIMAL(10,7) NOT NULL,
    longitud        DECIMAL(10,7) NOT NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 5. RUTA_PARADEROS (orden de paraderos por ruta)
-- =====================================================================
CREATE TABLE ruta_paraderos (
    id_ruta_paradero INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta          INT NOT NULL,
    id_paradero      INT NOT NULL,
    orden            INT NOT NULL,
    CONSTRAINT fk_rp_ruta FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE CASCADE,
    CONSTRAINT fk_rp_paradero FOREIGN KEY (id_paradero) REFERENCES paraderos(id_paradero) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 6. BUSES (FLOTA)
-- =====================================================================
CREATE TABLE buses (
    id_bus           INT AUTO_INCREMENT PRIMARY KEY,
    codigo           VARCHAR(10) NOT NULL UNIQUE,      -- ej. B-114
    placa            VARCHAR(10) NOT NULL UNIQUE,       -- ej. ABC-123
    modelo           VARCHAR(60) NOT NULL,
    anio_fabricacion YEAR NOT NULL,
    capacidad_pasajeros INT NOT NULL DEFAULT 45,
    id_ruta          INT NULL,
    id_conductor     INT NULL,
    id_empresa       INT NOT NULL,
    estado           ENUM('A_TIEMPO','DEMORADO','DESVIO','MANTENIMIENTO','SIN_CONDUCTOR','INACTIVO') NOT NULL DEFAULT 'INACTIVO',
    ubicacion_actual VARCHAR(120) NULL,
    latitud_actual   DECIMAL(10,7) NULL,
    longitud_actual  DECIMAL(10,7) NULL,
    velocidad_actual DECIMAL(5,2) NOT NULL DEFAULT 0,
    ocupacion_pct    INT NOT NULL DEFAULT 0,
    confianza_ml_pct INT NOT NULL DEFAULT 0,
    retraso_min      INT NOT NULL DEFAULT 0,
    ultima_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bus_ruta FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE SET NULL,
    CONSTRAINT fk_bus_conductor FOREIGN KEY (id_conductor) REFERENCES usuarios(id_usuario) ON DELETE SET NULL,
    CONSTRAINT fk_bus_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB;

-- =====================================================================
-- 7. HISTORIAL DE POSICIONES GPS
-- =====================================================================
CREATE TABLE posiciones_gps (
    id_posicion     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_bus          INT NOT NULL,
    latitud         DECIMAL(10,7) NOT NULL,
    longitud        DECIMAL(10,7) NOT NULL,
    velocidad       DECIMAL(5,2) NOT NULL,
    ocupacion_pct   INT NOT NULL,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pos_bus FOREIGN KEY (id_bus) REFERENCES buses(id_bus) ON DELETE CASCADE,
    INDEX idx_pos_bus_fecha (id_bus, fecha_hora)
) ENGINE=InnoDB;

-- =====================================================================
-- 8. ALERTAS / INCIDENCIAS
-- =====================================================================
CREATE TABLE alertas (
    id_alerta       INT AUTO_INCREMENT PRIMARY KEY,
    id_bus          INT NOT NULL,
    tipo            ENUM('RETRASO','DESVIO','EXCESO_VELOCIDAD','FALLA_MECANICA','PANICO','SOBRECUPO','OTRO') NOT NULL,
    descripcion     VARCHAR(255) NOT NULL,
    severidad       ENUM('BAJA','MEDIA','ALTA') NOT NULL DEFAULT 'MEDIA',
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atendida        TINYINT(1) NOT NULL DEFAULT 0,
    atendida_por    INT NULL,
    fecha_atencion  DATETIME NULL,
    CONSTRAINT fk_alerta_bus FOREIGN KEY (id_bus) REFERENCES buses(id_bus) ON DELETE CASCADE,
    CONSTRAINT fk_alerta_usuario FOREIGN KEY (atendida_por) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB;

-- =====================================================================
-- 9. DEMANDA DE PASAJEROS POR HORA
-- =====================================================================
CREATE TABLE demanda_horaria (
    id_demanda      INT AUTO_INCREMENT PRIMARY KEY,
    id_ruta         INT NULL,                 -- NULL = demanda agregada de toda la empresa
    id_empresa      INT NOT NULL,
    fecha           DATE NOT NULL,
    hora            TINYINT NOT NULL,          -- 0-23
    pasajeros       INT NOT NULL,
    nivel           ENUM('NORMAL','SATURADO','PICO') NOT NULL DEFAULT 'NORMAL',
    CONSTRAINT fk_demanda_ruta FOREIGN KEY (id_ruta) REFERENCES rutas(id_ruta) ON DELETE SET NULL,
    CONSTRAINT fk_demanda_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa)
) ENGINE=InnoDB;

-- =====================================================================
-- 10. MÉTRICAS DIARIAS (resumen para el panel principal)
-- =====================================================================
CREATE TABLE metricas_diarias (
    id_metrica          INT AUTO_INCREMENT PRIMARY KEY,
    id_empresa          INT NOT NULL,
    fecha                DATE NOT NULL,
    unidades_activas     INT NOT NULL,
    unidades_totales     INT NOT NULL,
    cumplimiento_pct     DECIMAL(5,2) NOT NULL,
    pasajeros_totales    INT NOT NULL,
    eta_promedio_min     INT NOT NULL,
    precision_ml_pct     DECIMAL(5,2) NOT NULL,
    CONSTRAINT fk_metrica_empresa FOREIGN KEY (id_empresa) REFERENCES empresas(id_empresa),
    UNIQUE KEY uq_metrica_empresa_fecha (id_empresa, fecha)
) ENGINE=InnoDB;

-- =====================================================================
-- 11. VISTA: estado de rutas con cumplimiento (usada por el panel)
-- =====================================================================
CREATE OR REPLACE VIEW vw_estado_rutas AS
SELECT
    r.id_ruta,
    r.codigo,
    r.nombre,
    r.origen,
    r.destino,
    r.id_empresa,
    COUNT(b.id_bus) AS total_buses,
    ROUND(AVG(CASE WHEN b.estado='A_TIEMPO' THEN 100
                   WHEN b.estado='DEMORADO' THEN 70
                   WHEN b.estado='DESVIO' THEN 45
                   ELSE 60 END),0) AS cumplimiento_pct,
    CASE
        WHEN SUM(CASE WHEN b.estado='DESVIO' THEN 1 ELSE 0 END) > 0 THEN 'Desvío'
        WHEN SUM(CASE WHEN b.estado='DEMORADO' THEN 1 ELSE 0 END) > 0 THEN 'Demorada'
        ELSE 'Normal'
    END AS estado_general
FROM rutas r
LEFT JOIN buses b ON b.id_ruta = r.id_ruta AND b.estado NOT IN ('INACTIVO','MANTENIMIENTO')
GROUP BY r.id_ruta, r.codigo, r.nombre, r.origen, r.destino, r.id_empresa;

-- =====================================================================
-- DATOS DE PRUEBA — EMPRESAS
-- =====================================================================
INSERT INTO empresas (nombre, ruc, telefono, direccion) VALUES
('Lipetsa S.A.',                 '20501234561', '01-4567890', 'Av. Argentina 2145, Lima'),
('Transportes San Cristóbal S.A.C.', '20501234562', '01-4567891', 'Av. Universitaria 890, Los Olivos'),
('Corredor Javier Prado E.I.R.L.', '20501234563', '01-4567892', 'Av. Javier Prado Este 4200, Ate'),
('Etuval Perú S.A.',              '20501234564', '01-4567893', 'Av. Elmer Faucett 320, Callao');

-- =====================================================================
-- DATOS DE PRUEBA — USUARIOS (coinciden con el login de demo)
--   Contraseña de todos: Lima2024
-- =====================================================================
INSERT INTO usuarios (nombre_completo, correo, contrasena, rol, iniciales, id_empresa, licencia_conducir) VALUES
('Eduardo López',   'operador@lipetsa.pe',  'Lima2024', 'OPERADOR',  'EL', 1, NULL),
('Carlos Ríos',     'conductor@lipetsa.pe', 'Lima2024', 'CONDUCTOR', 'CR', 1, 'Q12345678'),
('Admin Sistema',   'admin@lipetsa.pe',     'Lima2024', 'ADMIN',     'AS', 1, NULL),
('Milagros Torres', 'mtorres@sancristobal.pe','Lima2024','OPERADOR', 'MT', 2, NULL),
('Jhon Quispe',     'jquispe@sancristobal.pe','Lima2024','CONDUCTOR','JQ', 2, 'Q87654321'),
('Rosa Huamán',     'rhuaman@sancristobal.pe','Lima2024','CONDUCTOR','RH', 2, 'Q11223344'),
('Percy Salazar',   'psalazar@corredorjp.pe', 'Lima2024','CONDUCTOR','PS', 3, 'Q55667788'),
('Lucía Fernández', 'lfernandez@corredorjp.pe','Lima2024','OPERADOR','LF', 3, NULL),
('Julio Ramos',     'jramos@etuval.pe',      'Lima2024','CONDUCTOR', 'JR', 4, 'Q99887766'),
('Yesenia Paredes', 'yparedes@etuval.pe',    'Lima2024','ADMIN',     'YP', 4, NULL);

-- =====================================================================
-- DATOS DE PRUEBA — RUTAS
-- =====================================================================
INSERT INTO rutas (codigo, nombre, origen, destino, distancia_km, tiempo_estimado_min, tarifa, id_empresa) VALUES
('A12', 'Miraflores → San Juan de Lurigancho', 'Miraflores', 'San Juan de Lurigancho', 24.30, 65, 2.50, 1),
('C8',  'Callao → Surco',                       'Callao',     'Surco',                  28.10, 70, 2.50, 1),
('B3',  'Ate → Barranco',                        'Ate',        'Barranco',               19.80, 55, 2.00, 2),
('M1',  'Los Olivos → Surquillo',                'Los Olivos', 'Surquillo',              21.40, 60, 2.00, 2),
('J5',  'San Isidro → Chorrillos',               'San Isidro', 'Chorrillos',             16.90, 45, 2.00, 3),
('E2',  'Ventanilla → San Miguel',               'Ventanilla', 'San Miguel',             25.60, 68, 2.50, 4),
('P9',  'Comas → La Molina',                     'Comas',      'La Molina',              30.20, 80, 3.00, 2),
('R4',  'Villa El Salvador → Cercado de Lima',   'Villa El Salvador','Cercado de Lima',   22.70, 62, 2.50, 3);

-- =====================================================================
-- DATOS DE PRUEBA — BUSES (flota aleatoria de unidades peruanas)
-- =====================================================================
INSERT INTO buses
(codigo, placa, modelo, anio_fabricacion, capacidad_pasajeros, id_ruta, id_conductor, id_empresa,
 estado, ubicacion_actual, latitud_actual, longitud_actual, velocidad_actual, ocupacion_pct, confianza_ml_pct, retraso_min) VALUES
('B-114', 'AKQ-114', 'Volvo B270F',         2019, 60, 1, 2, 1, 'DEMORADO',     'Av. Javier Prado',       -12.0891, -77.0004, 18.0, 73, 92, 8),
('B-207', 'AKQ-207', 'Mercedes-Benz OF1721',2020, 50, 2, NULL, 1, 'A_TIEMPO',   'Av. La Marina, Callao',  -12.0562, -77.1181, 22.0, 55, 95, 0),
('B-039', 'BSY-039', 'Scania K310',         2018, 55, 3, NULL, 2, 'DESVIO',     'Av. La Marina',          -12.0731, -77.0876, 14.0, 88, 78, 12),
('B-155', 'BSY-155', 'Volvo B270F',         2021, 60, 4, NULL, 2, 'A_TIEMPO',   'Av. Universitaria, Los Olivos', -11.9820, -77.0790, 19.0, 62, 89, 0),
('B-088', 'CTL-088', 'Mercedes-Benz OF1721',2017, 50, 1, NULL, 1, 'A_TIEMPO',   'Av. Benavides, Surco',   -12.1181, -77.0090, 20.0, 45, 97, 0),
('B-301', 'CTL-301', 'Scania K310',         2016, 55, NULL, NULL, 1, 'MANTENIMIENTO', 'Taller Lipetsa', NULL, NULL, 0.0, 0, 0, 0),
('B-422', 'DHM-422', 'Volvo B270F',         2015, 60, NULL, NULL, 1, 'SIN_CONDUCTOR', 'Cochera Argentina', NULL, NULL, 0.0, 0, 0, 0),
('B-512', 'DHM-512', 'Mercedes-Benz OF1721',2022, 50, 5, 7, 3, 'A_TIEMPO',     'Av. Arequipa, San Isidro', -12.0968, -77.0349, 24.0, 40, 96, 0),
('B-618', 'EJP-618', 'Scania K310',         2019, 55, 6, 9, 4, 'DEMORADO',     'Av. Elmer Faucett',      -12.0245, -77.1053, 12.0, 81, 84, 6),
('B-729', 'EJP-729', 'Volvo B270F',         2020, 60, 7, NULL, 2, 'A_TIEMPO',   'Av. Túpac Amaru, Comas', -11.9525, -77.0602, 21.0, 58, 91, 0),
('B-833', 'FKQ-833', 'Mercedes-Benz OF1721',2018, 50, 8, NULL, 3, 'DESVIO',     'Av. Pachacútec, VES',    -12.2165, -76.9425, 10.0, 92, 70, 15);

-- =====================================================================
-- DATOS DE PRUEBA — POSICIONES GPS (historial reciente por bus)
-- =====================================================================
INSERT INTO posiciones_gps (id_bus, latitud, longitud, velocidad, ocupacion_pct, fecha_hora) VALUES
(1, -12.0910, -76.9980, 24.0, 65, NOW() - INTERVAL 30 MINUTE),
(1, -12.0901, -76.9992, 20.0, 70, NOW() - INTERVAL 20 MINUTE),
(1, -12.0891, -77.0004, 18.0, 73, NOW() - INTERVAL 5 MINUTE),
(2, -12.0570, -77.1160, 25.0, 50, NOW() - INTERVAL 25 MINUTE),
(2, -12.0562, -77.1181, 22.0, 55, NOW() - INTERVAL 5 MINUTE),
(3, -12.0715, -77.0850, 16.0, 85, NOW() - INTERVAL 15 MINUTE),
(3, -12.0731, -77.0876, 14.0, 88, NOW() - INTERVAL 3 MINUTE),
(4, -11.9800, -77.0770, 19.0, 60, NOW() - INTERVAL 10 MINUTE),
(4, -11.9820, -77.0790, 19.0, 62, NOW() - INTERVAL 2 MINUTE),
(5, -12.1190, -77.0100, 21.0, 44, NOW() - INTERVAL 8 MINUTE),
(5, -12.1181, -77.0090, 20.0, 45, NOW() - INTERVAL 1 MINUTE);

-- =====================================================================
-- DATOS DE PRUEBA — ALERTAS
-- =====================================================================
INSERT INTO alertas (id_bus, tipo, descripcion, severidad, fecha_hora, atendida) VALUES
(1, 'RETRASO',        'Retraso de 8 minutos detectado por el modelo predictivo en Av. Javier Prado', 'MEDIA', NOW() - INTERVAL 8 MINUTE, 0),
(3, 'DESVIO',         'Desvío activo por congestión vehicular en Av. La Marina', 'ALTA', NOW() - INTERVAL 12 MINUTE, 0),
(9, 'RETRASO',        'Retraso de 6 minutos por tráfico en Av. Elmer Faucett', 'MEDIA', NOW() - INTERVAL 6 MINUTE, 0),
(11,'SOBRECUPO',      'Ocupación al 92%, posible sobrecupo en Av. Pachacútec', 'ALTA', NOW() - INTERVAL 15 MINUTE, 0),
(2, 'EXCESO_VELOCIDAD','Velocidad registrada de 68 km/h en tramo urbano', 'BAJA', NOW() - INTERVAL 2 DAY, 1);

-- =====================================================================
-- DATOS DE PRUEBA — DEMANDA HORARIA (hoy, empresa 1: Lipetsa)
-- =====================================================================
INSERT INTO demanda_horaria (id_ruta, id_empresa, fecha, hora, pasajeros, nivel) VALUES
(NULL, 1, CURDATE(), 6,  180, 'NORMAL'),
(NULL, 1, CURDATE(), 7,  430, 'SATURADO'),
(NULL, 1, CURDATE(), 8,  598, 'PICO'),
(NULL, 1, CURDATE(), 9,  405, 'SATURADO'),
(NULL, 1, CURDATE(), 10, 228, 'NORMAL'),
(NULL, 1, CURDATE(), 11, 175, 'NORMAL'),
(NULL, 1, CURDATE(), 12, 330, 'SATURADO'),
(NULL, 1, CURDATE(), 13, 252, 'NORMAL');

-- =====================================================================
-- DATOS DE PRUEBA — MÉTRICAS DIARIAS
-- =====================================================================
INSERT INTO metricas_diarias (id_empresa, fecha, unidades_activas, unidades_totales, cumplimiento_pct, pasajeros_totales, eta_promedio_min, precision_ml_pct) VALUES
(1, CURDATE(), 5, 7, 83.00, 2418, 7, 89.00),
(2, CURDATE(), 3, 4, 78.50, 1560, 9, 86.00),
(3, CURDATE(), 2, 2, 65.00, 980,  11, 81.00),
(4, CURDATE(), 1, 1, 74.00, 540,  8, 84.00);

-- =====================================================================
-- FIN DEL SCRIPT
-- =====================================================================
