import React from 'react';
import { Input } from '../ui/input';

interface DateInputProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

export function DateInput({ value, onChange, placeholder }: DateInputProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    let rawValue = e.target.value.replace(/\D/g, '');
    
    if (rawValue.length <= 8) {
      let formatted = rawValue;
      if (rawValue.length >= 2) {
        formatted = rawValue.slice(0, 2) + '/' + rawValue.slice(2);
      }
      if (rawValue.length >= 4) {
        formatted = rawValue.slice(0, 2) + '/' + rawValue.slice(2, 4) + '/' + rawValue.slice(4);
      }
      onChange(formatted);
    }
  };

  return (
    <Input
      type="text"
      value={value}
      onChange={handleChange}
      placeholder={placeholder || 'dd/mm/aaaa'}
      maxLength={10}
    />
  );
}


