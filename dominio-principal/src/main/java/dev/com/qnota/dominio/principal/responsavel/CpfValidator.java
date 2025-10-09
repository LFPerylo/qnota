package dev.com.qnota.dominio.principal.responsavel;

final class CpfValidator {
    private CpfValidator() {}

    static boolean formatoValido(String cpf) {
        return cpf != null && cpf.replaceAll("\\D", "").matches("\\d{11}");
    }
    static boolean valido(String cpf) {
        if (!formatoValido(cpf)) return false;
        String s = cpf.replaceAll("\\D", "");
        if (s.chars().distinct().count() == 1) return false;
        return dig(s, 9) == (s.charAt(9) - '0') && dig(s, 10) == (s.charAt(10) - '0');
    }
    private static int dig(String s, int pos) {
        int soma = 0, peso = pos + 1;
        for (int i = 0; i < pos; i++) soma += (s.charAt(i) - '0') * (peso--);
        int r = 11 - (soma % 11);
        return (r >= 10) ? 0 : r;
    }
}
