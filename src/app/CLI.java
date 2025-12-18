package app;

import domain.Aluno;

import java.sql.SQLException;
import java.util.Scanner;

public class CLI {
    private final UseCases uc;
    public CLI(UseCases uc) { this.uc = uc; }
    private final String BASE_RECURSOS = "scriptsSQL/";

    public void start() {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("""
                ===== TP3 - B-Tree + DB =====
                1) Cadastrar aluno
                2) Buscar por matrícula
                3) Remover por matrícula
                4) Listar por faixa
                5) Exportar B-Tree -> Txt
                6) Visualizar B-Tree (InOrder)
                7) Visualizar B-Tree (Pretty)
                8) Salvar BTree -> CSV
                9) Carregar CSV -> BTree
                10) Recarregar índice a partir do DB
                11) Carregar SCRIPT SQL (repopular banco)
                0) Sair
            """);
            System.out.print("Escolha: ");
            String op = sc.nextLine().trim();
            switch (op) {
                case "1" -> cadastrar(sc);
                case "2" -> buscarPorMatricula(sc);
                case "3" -> removerPorMatricula(sc);
                case "5" -> exportarPretty(sc);
                case "4" -> listarFaixa(sc);
                case "6" -> System.out.println(uc.visualizarArvoreInOrder());
                case "7" -> System.out.println(uc.visualizarArvorePretty());
                case "8" -> salvarIndiceCSV(sc);
                case "9" -> carregarIndiceCSV(sc);
                case "0" -> { System.out.println("Saindo..."); return; }
                case "10" -> { uc.reconstruirIndice();
                    System.out.println("Índice recarregado do banco.");
                }
                case "11" -> carregarScriptSQL(sc);
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void listarFaixa(Scanner sc) {
        try {
            System.out.print("Matrícula inicial (long): ");
            long a = Long.parseLong(sc.nextLine().trim());
            System.out.print("Matrícula final (long): ");
            long b = Long.parseLong(sc.nextLine().trim());
            var lista = uc.listarFaixa(a, b);
            if (lista.isEmpty()) {
                System.out.println("Nenhum aluno na faixa.");
                return;
            }
            for (var al : lista) {
                System.out.printf("Aluno{id=%d, mat=%d, nome=%s, curso=%s, email=%s, ativo=%s}%n",
                        al.getId(), al.getMatricula(), al.getNome(), al.getCurso(), al.getEmail(), al.isAtivo());
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exportarPretty(Scanner sc) {
        try {
            System.out.print("Caminho do arquivo (ex: btree.txt): ");
            String path = sc.nextLine().trim();
            if (path.isBlank()) path = "btree.txt";
            uc.exportarPretty(path);
            System.out.println("Exportado em: " + path);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }


    private void cadastrar(Scanner sc) {
        try {
            System.out.print("Matrícula (10 dígitos): ");
            long mat = Long.parseLong(sc.nextLine().trim());
            System.out.print("Nome: ");
            String nome = sc.nextLine();
            System.out.print("Curso: ");
            String curso = sc.nextLine();
            System.out.print("Email (opcional): ");
            String email = sc.nextLine();

            Aluno a = new Aluno(mat, nome, curso, email.isBlank() ? null : email);
            long id = uc.cadastrarAluno(a);
            System.out.println("Aluno cadastrado. id=" + id);

        } catch (NumberFormatException nfe) {
            System.out.println("Erro: matrícula deve conter apenas números.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Erro: " + ex.getMessage());
        } catch (Exception e) {
            System.out.println("Erro inesperado: " + e.getMessage());
        }
    }

    private void buscarPorMatricula(Scanner sc) {
        try {
            System.out.print("Matrícula: ");
            long m = Long.parseLong(sc.nextLine());            // <- long
            var res = uc.buscarPorMatricula(m);
            System.out.println(res.map(a ->
                    String.format("Aluno{id=%d, mat=%d, nome=%s, curso=%s, email=%s, ativo=%s}",
                            a.getId(), a.getMatricula(), a.getNome(), a.getCurso(), a.getEmail(), a.isAtivo())
            ).orElse("Não encontrado."));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void removerPorMatricula(Scanner sc) {
        try {
            System.out.print("Matrícula: ");
            long m = Long.parseLong(sc.nextLine().trim());
            boolean ok = uc.removerPorMatricula(m);
            System.out.println(ok ? "Aluno removido." : "Não foi possível remover.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void salvarIndiceCSV(Scanner sc) {
        try {
            System.out.print("Arquivo (ex: indices/btree_index.csv): ");
            String path = sc.nextLine().trim();
            if (path.isBlank()) path = "indices/btree_index.csv";
            java.nio.file.Files.createDirectories(java.nio.file.Path.of(path).getParent());
            uc.salvarIndiceCSV(path);
            System.out.println("Índice salvo em: " + path);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void carregarIndiceCSV(Scanner sc) {
        try {
            System.out.print("Arquivo (ex: indices/btree_index.csv): ");
            String path = sc.nextLine().trim();
            if (path.isBlank()) path = "indices/btree_index.csv";
            uc.carregarIndiceCSV(path);
            System.out.println("Índice carregado de: " + path);
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void carregarScriptSQL(Scanner sc) {
        try {
            String path = escolherArquivoDeRecursos(sc);
            if (path.isBlank()) {
                System.out.println("Caminho inválido.");
                return;
            }
            System.out.print("Limpar tabela 'aluno' antes? (s/n): ");
            boolean limpar = sc.nextLine().trim().equalsIgnoreCase("s");

            int count = uc.carregarScriptSQL(path, limpar);
            System.out.println("Script executado. Índice reconstruído. "
                    + (count > 0 ? ("Entradas no índice: " + count) : ""));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private String escolherArquivoDeRecursos(Scanner sc) {
        String[] arquivos = listarArquivosClasspath(BASE_RECURSOS);
        if (arquivos == null || arquivos.length == 0) {
            System.out.println("[AVISO] Nenhum arquivo encontrado em " + BASE_RECURSOS + " (classpath).");
            return null;
        }
        System.out.println("\nArquivos disponíveis:");
        for (int i = 0; i < arquivos.length; i++) {
            System.out.println("  " + (i + 1) + ") " + arquivos[i]);
        }
        System.out.print("Escolha um número: ");
        int idx;
        try { idx = Integer.parseInt(sc.nextLine().trim()) - 1; }
        catch (NumberFormatException e) { System.out.println("[AVISO] Entrada inválida."); return null; }
        if (idx < 0 || idx >= arquivos.length) { System.out.println("[AVISO] Número fora do intervalo."); return null; }
        return BASE_RECURSOS + arquivos[idx];
    }

    private String[] listarArquivosClasspath(String pastaClasspath){
        try {
            java.net.URL url = Thread.currentThread()
                    .getContextClassLoader()
                    .getResource(pastaClasspath);
            if (url == null) return null;

            java.nio.file.Path dir = java.nio.file.Paths.get(url.toURI());
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(dir)) {
                return stream
                        .filter(java.nio.file.Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .filter(n -> n.endsWith(".sql"))
                        .sorted()
                        .toArray(String[]::new);
            }
        }catch (Exception e){
            return null;
        }
    }
    

}
