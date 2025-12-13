-- Adiciona coluna formula_calculo na tabela simulados
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'qnota' 
        AND table_name = 'simulados' 
        AND column_name = 'formula_calculo'
    ) THEN
        ALTER TABLE qnota.simulados 
        ADD COLUMN formula_calculo TEXT NOT NULL DEFAULT 'PONDERADA';
    END IF;
END $$;

-- Adiciona constraint para validar valores permitidos (se não existir)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_schema = 'qnota' 
        AND table_name = 'simulados' 
        AND constraint_name = 'ck_sim_formula_calculo'
    ) THEN
        ALTER TABLE qnota.simulados
        ADD CONSTRAINT ck_sim_formula_calculo 
        CHECK (formula_calculo IN ('PONDERADA', 'ARITMETICA'));
    END IF;
END $$;

