# Relatório Técnico - Teste de Caixa Branca e Revisão de Código

## Introdução
Esta atividade tem como objetivo aplicar os conceitos de Teste de Caixa Branca, revisão estática e análise de fluxo de execução em um código-fonte Java legado. O sistema analisado consiste em um módulo de autenticação simples que se conecta a um banco de dados MySQL para validar credenciais de usuários. A análise buscou identificar falhas estruturais, vulnerabilidades de segurança e propor melhorias baseadas em boas práticas de Engenharia de Software.

## Análise Estática do Código
Durante a inspeção do arquivo original (`User.java`), foram identificadas diversas não conformidades críticas, detalhadas na planilha de Plano de Testes. Os principais problemas incluem:
* **Segurança (Vulnerabilidades):** O código original estava suscetível a ataques de *SQL Injection* devido à concatenação direta de Strings para formar a instrução SQL. Além disso, as senhas estavam sendo tratadas em texto limpo.
* **Conexões e Recursos:** Ausência de fechamento de conexões (`Connection`, `Statement`, `ResultSet`), gerando *resource leaks* que podem esgotar o pool do banco de dados.
* **Tratamento de Exceções:** Uso de blocos `catch` vazios (*swallowing exceptions*), ocultando falhas críticas de infraestrutura e dificultando o rastreamento de bugs.
* **Legibilidade e Organização:** Falta total de documentação (Javadoc), nomenclatura de variáveis globais que quebram o encapsulamento (`nome`, `result`) e violação do princípio de responsabilidade única.
* **Riscos Estruturais:** Risco iminente de `NullPointerException` caso o método de conexão falhasse, pois o sistema tentava criar um `Statement` a partir de uma conexão nula sem validação prévia.

## Grafo de Fluxo
Abaixo está a representação estrutural do método `verificarUsuario`, mapeando os caminhos lógicos e pontos de decisão do código original.

![Grafo de Fluxo do Método](<img width="444" height="744" alt="Grafo" src="https://github.com/user-attachments/assets/2d9dac0c-213d-464f-b197-82c41909e8a6" />
)

## Complexidade Ciclomática
Para determinar a quantidade de caminhos independentes e a base para os casos de teste, aplicou-se a fórmula da Complexidade Ciclomática:
**V(G) = E - N + 2P**

Onde:
* **E (Arestas):** 7
* **N (Nós):** 6
* **P (Componentes conectados):** 1

**Cálculo:**
V(G) = 7 - 6 + 2(1)
V(G) = 1 + 2
**V(G) = 3**

## Caminhos Básicos
Com base na complexidade ciclomática calculada, foram identificados 3 caminhos básicos que garantem a cobertura total do método estrutural:

* **Caminho 1 (Sucesso na Autenticação - 1 -> 2 -> 3 -> 4 -> 6):** O sistema entra no bloco `try`, executa a query sem erros no banco, o `ResultSet` encontra o usuário/senha (avaliando o `if` como verdadeiro), atualiza o status para `true` e retorna o resultado.
* **Caminho 2 (Falha de Autenticação / Não Encontrado - 1 -> 2 -> 3 -> 6):** O sistema executa a query sem erros, mas os dados não batem. O `ResultSet` retorna falso no `if`, o fluxo pula o corpo da condicional e retorna o status inicial `false`.
* **Caminho 3 (Exceção Crítica - 1 -> 2 -> 5 -> 6):** Ocorre uma falha de infraestrutura (banco fora do ar ou conexão nula) ou sintaxe SQL inválida. Uma exceção é lançada, o fluxo desvia para o `catch` ignorando o resto, e o método retorna `false`.

## Melhorias Implementadas
Na refatoração (classe `AutenticacaoService.java`), as seguintes melhorias foram aplicadas:
* Substituição do `Statement` por `PreparedStatement` para proteção contra *SQL Injection*.
* Implementação da estrutura `try-with-resources` para garantir o fechamento automático e seguro do banco de dados, prevenindo vazamentos de memória.
* Remoção de variáveis públicas globais, garantindo o encapsulamento e evitando problemas de concorrência.
* Inserção de Javadoc em toda a classe para melhorar a manutenibilidade.
* Remoção do `Class.forName` depreciado na instanciação do driver JDBC.
* Adição de tratamento adequado no `catch` lançando o erro via `System.err.println` e `e.printStackTrace()`.

## Conclusão
A realização desta atividade evidenciou a importância do teste estrutural (Caixa Branca) aliado à revisão por pares (análise estática). O código original, embora pudesse compilar e "funcionar" em um cenário ideal, era estruturalmente frágil e perigoso para um ambiente de produção. O maior desafio do processo de refatoração foi garantir que as regras de negócio fossem mantidas intactas enquanto a segurança estrutural era adicionada. A aplicação da complexidade ciclomática provou ser uma ferramenta analítica excelente para prever cenários de teste necessários, garantindo que nenhuma linha crítica do fluxo ou exceção fique sem cobertura, elevando consideravelmente a qualidade do software entregue.
