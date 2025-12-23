package de.hft_stuttgart.ip1;

import java.io.IOException;

public class ScriptTokenizer {
    private final String input;
    private int index = 0;
    private Token cached;

    public ScriptTokenizer(String input) {
        this.input = input;
    }

    public Token peek() throws IOException {
        if (cached == null) {
            cached = nextToken();
        }
        return cached;
    }

    public Token next() throws IOException {
        if (cached != null) {
            Token token = cached;
            cached = null;
            return token;
        }
        return nextToken();
    }

    private Token nextToken() throws IOException {
        skipWhitespace();
        if (index >= input.length()) {
            return new Token(TokenType.EOF, "");
        }
        char ch = input.charAt(index);
        if (ch == '"') {
            return readString();
        }
        if (Character.isDigit(ch) || ch == '-' && index + 1 < input.length() && Character.isDigit(input.charAt(index + 1))) {
            return readNumber();
        }
        if (Character.isLetter(ch) || ch == '_' || ch == '@') {
            return readWord();
        }
        index++;
        return new Token(TokenType.SYMBOL, String.valueOf(ch));
    }

    private void skipWhitespace() {
        while (index < input.length()) {
            char ch = input.charAt(index);
            if (Character.isWhitespace(ch)) {
                index++;
                continue;
            }
            break;
        }
    }

    private Token readString() throws IOException {
        if (input.startsWith("\"\"\"", index)) {
            index += 3;
            StringBuilder builder = new StringBuilder();
            while (index < input.length()) {
                if (input.startsWith("\"\"\"", index)) {
                    index += 3;
                    return new Token(TokenType.STRING, normalizeTriple(builder.toString()));
                }
                char ch = input.charAt(index++);
                if (ch == '\\') {
                    builder.append(readEscape());
                } else {
                    builder.append(ch);
                }
            }
            throw new IOException("Unterminated triple-quoted string");
        }

        index++;
        StringBuilder builder = new StringBuilder();
        while (index < input.length()) {
            char ch = input.charAt(index++);
            if (ch == '"') {
                return new Token(TokenType.STRING, builder.toString());
            }
            if (ch == '\\') {
                builder.append(readEscape());
            } else {
                builder.append(ch);
            }
        }
        throw new IOException("Unterminated string");
    }

    private char readEscape() throws IOException {
        if (index >= input.length()) {
            throw new IOException("Unterminated escape sequence");
        }
        char escaped = input.charAt(index++);
        return switch (escaped) {
            case 'n' -> '\n';
            case '"' -> '"';
            case '\\' -> '\\';
            default -> throw new IOException("Unsupported escape: \\" + escaped);
        };
    }

    private String normalizeTriple(String text) {
        return text.replace("\r", "").replace("\n", " ");
    }

    private Token readNumber() {
        int start = index;
        index++;
        while (index < input.length()) {
            char ch = input.charAt(index);
            if (!Character.isDigit(ch) && ch != '.') {
                break;
            }
            index++;
        }
        return new Token(TokenType.NUMBER, input.substring(start, index));
    }

    private Token readWord() {
        int start = index;
        index++;
        while (index < input.length()) {
            char ch = input.charAt(index);
            if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-' && ch != '*') {
                break;
            }
            index++;
        }
        return new Token(TokenType.WORD, input.substring(start, index));
    }
}
