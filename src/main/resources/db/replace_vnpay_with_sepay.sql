SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF NOT EXISTS (SELECT 1 FROM payment_methods WHERE UPPER(name) = 'SEPAY')
BEGIN
    INSERT INTO payment_methods (name, description)
    VALUES ('SEPAY', N'Chuyển khoản QR qua SePay');
END;

DELETE FROM payment_methods
WHERE UPPER(name) = 'VNPAY'
  AND NOT EXISTS (SELECT 1 FROM payments WHERE payment_method_id = payment_methods.id);

IF NOT EXISTS (
    SELECT 1 FROM sys.indexes
    WHERE name = 'ux_payments_transaction_id' AND object_id = OBJECT_ID('payments')
)
BEGIN
    CREATE UNIQUE INDEX ux_payments_transaction_id ON payments(transaction_id)
    WHERE transaction_id IS NOT NULL;
END;

COMMIT TRANSACTION;
