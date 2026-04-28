-- añadir las columnas necesarias para el bloqueo de cuenta
ALTER TABLE empleados
    ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0,
    ADD COLUMN bloqueado         TINYINT(1) NOT NULL DEFAULT 0;

-- trigger que se ejecuta automaticamente antes de cada UPDATE en la tabla empleados
-- si el nuevo valor de intentos_fallidos llega a 5, pone bloqueado = 1 solo
DELIMITER //
CREATE TRIGGER trg_bloquear_cuenta
BEFORE UPDATE ON empleados
FOR EACH ROW
BEGIN
    IF NEW.intentos_fallidos >= 5 THEN
        SET NEW.bloqueado = 1;
    END IF;
END //
DELIMITER ;
