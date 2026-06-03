package login;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Classe responsável pela autenticação de usuários no sistema.
 * Refatorada para aplicar boas práticas de segurança, código limpo e tratamento de exceções.
 */
public class AutenticacaoService {

    // Variável encapsulada, removida do escopo público global
    private String nomeUsuarioLogado;

    /**
     * Estabelece a conexão com o banco de dados.
     * @return Objeto Connection com a conexão ativa.
     * @throws SQLException Caso ocorra erro de infraestrutura na conexão.
     */
    private Connection conectarBD() throws SQLException {
        String url = "jdbc:mysql://127.0.0.1/test?user=lopes&password=123";
        // O JDBC moderno dispensa o uso do Class.forName()
        return DriverManager.getConnection(url);
    }

    /**
     * Verifica se as credenciais do usuário são válidas no banco de dados.
     * @param login O login do usuário inserido no sistema.
     * @param senha A senha em texto (em um cenário real, deve ser validada usando Hash).
     * @return true se as credenciais estiverem corretas, false caso contrário.
     */
    public boolean verificarUsuario(String login, String senha) {
        boolean isAutenticado = false;
        String sql = "SELECT nome FROM usuarios WHERE login = ? AND senha = ?";

        // O bloco try-with-resources garante que conn e stmt sejam fechados automaticamente,
        // prevenindo vazamento de memória (resource leak).
        try (Connection conn = conectarBD();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Substituição das variáveis para prevenir SQL Injection
            stmt.setString(1, login);
            stmt.setString(2, senha);

            // O ResultSet também é fechado automaticamente neste try aninhado
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    isAutenticado = true;
                    this.nomeUsuarioLogado = rs.getString("nome");
                }
            }

        } catch (SQLException e) {
            // Tratamento correto da exceção: registrando o erro no console em vez de silenciá-lo
            System.err.println("Falha na operação de banco de dados: " + e.getMessage());
            e.printStackTrace();
        }

        return isAutenticado;
    }

    /**
     * Retorna o nome do usuário após a autenticação com sucesso.
     */
    public String getNomeUsuarioLogado() {
        return nomeUsuarioLogado;
    }
}
