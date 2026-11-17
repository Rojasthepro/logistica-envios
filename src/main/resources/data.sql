-- =====================================================================
-- Datos de demostración. Los casos que rompen reglas están marcados.
-- =====================================================================

-- 4 zonas: 2 cubiertas y 2 sin cobertura
INSERT INTO zona_cobertura (id_zona, nombre, cubierta, tarifa_base, valor_por_kg) VALUES
(1, 'Bogotá',   TRUE,   8000.00, 1500.00),
(2, 'Medellín', TRUE,   9000.00, 1700.00),
(3, 'Leticia',  FALSE, 25000.00, 4000.00),   -- sin cobertura (RN-02)
(4, 'Mitú',     FALSE, 30000.00, 4500.00);   -- sin cobertura (RN-02)

-- 12 remitentes: 10 activos y 2 inactivos
INSERT INTO remitente (id_remitente, nombre, documento, telefono, direccion, ciudad, estado) VALUES
(1,  'Laura Gómez Rincón',        '1010101010', '3001234567', 'Calle 45 # 12-30',   'Bogotá',   'ACTIVO'),   -- caso feliz
(2,  'Carlos Pérez Mejía',        '1020202020', '3109876543', 'Carrera 7 # 72-15',  'Bogotá',   'ACTIVO'),   -- PKG-7 de 35 kg (RN-01)
(3,  'Distribuciones Andina SAS', '900123456',  '6044445566', 'Calle 10 # 43-20',   'Medellín', 'ACTIVO'),   -- PKG-5 de 160 cm (RN-01)
(4,  'Ana María Ruiz',            '1040404040', '3157778899', 'Avenida 68 # 24-10', 'Bogotá',   'ACTIVO'),   -- PKG-8 a Leticia (RN-02)
(5,  'Jorge Castaño Vélez',       '71050505',   '3002223344', 'Carrera 70 # 44-12', 'Medellín', 'ACTIVO'),   -- tiene GUIA-000001 y PKG-10 RECOGIDO
(6,  'Papelería El Punto',        '900555666',  '6017654321', '',                   'Bogotá',   'ACTIVO'),   -- sin dirección (RN-03)
(7,  'Marcela Ortiz',             '1070707070', '3014445566', 'Calle 80 # 20-05',   'Bogotá',   'ACTIVO'),
(8,  'Ferretería La Llave',       '900777888',  '6019998877', 'Calle 13 # 30-40',   'Bogotá',   'ACTIVO'),
(9,  'Santiago Restrepo',         '1090909090', '3046667788', 'Calle 33 # 65-10',   'Medellín', 'ACTIVO'),
(10, 'Luisa Fernanda Mora',       '1100110011', '3123334455', 'Carrera 15 # 93-60', 'Bogotá',   'ACTIVO'),   -- sin paquetes (estado vacío)
(11, 'Pedro Salazar',             '1111111111', '3201112233', 'Calle 26 # 50-12',   'Bogotá',   'INACTIVO'), -- inactivo con paquetes (RN-03)
(12, 'Comercial El Retiro',       '900999000',  '6042221100', 'Carrera 43A # 1-50', 'Medellín', 'INACTIVO');

-- 8 destinatarios
INSERT INTO destinatario (id_destinatario, nombre, telefono, direccion, ciudad, id_zona) VALUES
(1, 'Sofía Herrera',     '3005556677', 'Calle 100 # 15-20',    'Bogotá',   1),
(2, 'Andrés Gil',        '3116667788', 'Carrera 11 # 82-30',   'Bogotá',   1),
(3, 'Clínica Santa Fe',  '6016030303', 'Calle 119 # 7-75',     'Bogotá',   1),
(4, 'Valentina Cano',    '3127778899', 'Calle 50 # 46-36',     'Medellín', 2),
(5, 'Tienda Mundo Bebé', '6045112233', 'Carrera 80 # 30-15',   'Medellín', 2),
(6, 'Julián Arango',     '3138889900', 'Calle 10 Sur # 50-28', 'Medellín', 2),
(7, 'Rosa Tikuna',       '3149990011', 'Calle 8 # 10-35',      'Leticia',  3),   -- zona sin cobertura
(8, 'Hernán Cubeo',      '3150001122', 'Carrera 14 # 12-02',   'Mitú',     4);   -- zona sin cobertura

-- 5 mensajeros (Sandra Ríos ya está EN_RUTA con la GUIA-000001)
INSERT INTO mensajero (id_mensajero, nombre, documento, zona, estado) VALUES
(1, 'Andrés Torres', '79111222', 'Bogotá',   'DISPONIBLE'),
(2, 'Diana López',   '52333444', 'Bogotá',   'DISPONIBLE'),
(3, 'Felipe Mora',   '71555666', 'Medellín', 'DISPONIBLE'),
(4, 'Sandra Ríos',   '43777888', 'Medellín', 'EN_RUTA'),
(5, 'Miguel Díaz',   '80999000', 'Bogotá',   'DISPONIBLE');

-- Recolección previa: explica el PKG-10 RECOGIDO y hace que la próxima guía sea GUIA-000002
-- costo = 9000 + max(3, 30*30*30/5000 = 5.4) * 1700 = 18180
INSERT INTO recoleccion (id_recoleccion, numero_guia, fecha_hora, direccion_recoleccion, costo_total, estado,
                         id_remitente, id_mensajero, usuario_operador, sello_tiempo) VALUES
(1, 'GUIA-000001', '2026-10-27 09:30:00', 'Carrera 70 # 44-12, Medellín', 18180.00, 'REALIZADA',
 5, 4, 'operador.demo', '2026-10-27 09:30:00');

-- 20 paquetes
INSERT INTO paquete (id_paquete, descripcion, peso_kg, alto_cm, ancho_cm, largo_cm, valor_declarado, estado,
                     id_destinatario, id_recoleccion, id_remitente) VALUES
(1,  'Libros universitarios',    2.50, 30, 20, 15,  150000.00, 'REGISTRADO', 1, NULL, 1),
(2,  'Celular en caja',          0.80, 20, 15, 10, 1200000.00, 'REGISTRADO', 4, NULL, 1),
(3,  'Horno microondas',        10.00, 50, 40, 30,  450000.00, 'REGISTRADO', 2, NULL, 1),  -- peso volumétrico 12 kg > 10 kg
(4,  'Repuestos de moto',        6.00, 40, 30, 30,  300000.00, 'REGISTRADO', 5, NULL, 3),
(5,  'Rollo de tela industrial', 8.00, 70, 50, 40,  500000.00, 'REGISTRADO', 4, NULL, 3),  -- 160 cm sumados (RN-01)
(6,  'Documentos notariales',    0.50,  5, 25, 35,   50000.00, 'REGISTRADO', 1, NULL, 2),
(7,  'Bulto de café',           35.00, 60, 40, 40,  400000.00, 'REGISTRADO', 3, NULL, 2),  -- 35 kg (RN-01)
(8,  'Medicamentos',             1.50, 20, 20, 20,  250000.00, 'REGISTRADO', 7, NULL, 4),  -- Leticia sin cobertura (RN-02)
(9,  'Ropa de bebé',             1.00, 25, 20, 15,  120000.00, 'REGISTRADO', 2, NULL, 4),
(10, 'Zapatos deportivos',       3.00, 30, 30, 30,  350000.00, 'RECOGIDO',   4, 1,    5),  -- ya RECOGIDO (RN-05)
(11, 'Lámpara de mesa',          2.00, 40, 25, 25,   90000.00, 'REGISTRADO', 5, NULL, 5),
(12, 'Juego de sábanas',         1.80, 35, 30, 10,  110000.00, 'REGISTRADO', 6, NULL, 5),
(13, 'Resmas de papel',         12.00, 45, 30, 25,  140000.00, 'REGISTRADO', 1, NULL, 6),
(14, 'Perfume importado',        0.60, 15, 10, 10,  380000.00, 'REGISTRADO', 3, NULL, 7),
(15, 'Kit escolar',              2.20, 30, 25, 20,   95000.00, 'REGISTRADO', 8, NULL, 7),  -- Mitú sin cobertura (RN-02)
(16, 'Taladro percutor',         4.50, 40, 30, 15,  320000.00, 'REGISTRADO', 2, NULL, 8),
(17, 'Audífonos',                0.40, 15, 15, 10,  210000.00, 'REGISTRADO', 6, NULL, 9),
(18, 'Cámara fotográfica',       1.20, 20, 20, 15, 2100000.00, 'REGISTRADO', 1, NULL, 11), -- remitente INACTIVO
(19, 'Tablet',                   0.90, 25, 20,  5,  900000.00, 'REGISTRADO', 2, NULL, 11), -- remitente INACTIVO
(20, 'Herramientas de jardín',   7.00, 50, 30, 20,  180000.00, 'REGISTRADO', 5, NULL, 9);

-- Como se insertaron ids explícitos, se reinician los contadores de identidad
ALTER TABLE zona_cobertura ALTER COLUMN id_zona RESTART WITH 5;
ALTER TABLE remitente ALTER COLUMN id_remitente RESTART WITH 13;
ALTER TABLE destinatario ALTER COLUMN id_destinatario RESTART WITH 9;
ALTER TABLE mensajero ALTER COLUMN id_mensajero RESTART WITH 6;
ALTER TABLE recoleccion ALTER COLUMN id_recoleccion RESTART WITH 2;
ALTER TABLE paquete ALTER COLUMN id_paquete RESTART WITH 21;
