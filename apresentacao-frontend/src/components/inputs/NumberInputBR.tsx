import React from 'react';
import { Input } from '../ui/input';

interface NumberInputBRProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  max?: number;
}

function formatNumberInput(value: string): string {
  // Remove tudo exceto números e vírgula
  let cleaned = value.replace(/[^\d,]/g, '');
  
  // Permite apenas uma vírgula
  const parts = cleaned.split(',');
  if (parts.length > 2) {
    cleaned = parts[0] + ',' + parts.slice(1).join('');
  }
  
  // Limita a 2 casas decimais
  if (parts.length === 2 && parts[1].length > 2) {
    cleaned = parts[0] + ',' + parts[1].substring(0, 2);
  }
  
  return cleaned;
}

export function NumberInputBR({ value, onChange, placeholder, max = 10 }: NumberInputBRProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatNumberInput(e.target.value);
    
    // Verifica se o valor não excede o máximo
    const numValue = parseFloat(formatted.replace(',', '.'));
    if (!isNaN(numValue) && numValue > max) {
      return;
    }
    
    onChange(formatted);
  };

  return (
    <Input
      type="text"
      value={value}
      onChange={handleChange}
      placeholder={placeholder || '0,00'}
    />
  );
}


