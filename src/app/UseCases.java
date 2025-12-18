package app;

import domain.Aluno;
import index.BTree;
import index.KeyPointer;
import storage.StudentRepository;

import java.util.Optional;

public class UseCases {
    private BTree<Long, KeyPointer> index;
    private final StudentRepository repo;

    public UseCases(BTree index, StudentRepository repo) {
        this.index = index;
        this.repo = repo;
    }
    public String visualizarArvorePretty() { return index.printPretty(); }
    public void exportarPretty(String path) { index.savePrettyToFile(path); }


    // regra: matrícula deve ter exatamente 10 dígitos (long)
    private boolean matriculaValida10(long m) {
        return m >= 1_000_000_000L && m <= 9_999_999_999L;
    }

    public long cadastrarAluno(Aluno a) {
        if (!matriculaValida10(a.getMatricula()))
            throw new IllegalArgumentException("Matrícula inválida: use 10 dígitos numéricos.");

        long id = repo.save(a);
        if (id == -2L) { // duplicado no DB
            throw new IllegalStateException("Matrícula já cadastrada.");
        }
        if (id <= 0) {
            throw new IllegalStateException("Falha ao cadastrar no banco.");
        }
        // DB ok -> indexa
        index.insert(a.getMatricula(), id);
        return id;
    }


    public Optional<Aluno> buscarPorMatricula(long m) {  // <- long
        var ptr = index.search(m);
        return ptr.isEmpty() ? Optional.empty() : repo.findById(ptr.get());
    }

    // app/UseCases.java
    public void reconstruirIndice() {
        index.clear(); // começa limpo
        for (var par : repo.listarChaves()) {
            long matricula = par[0];
            long id = par[1];
            index.insert(matricula, id);
        }
    }

    public java.util.List<domain.Aluno> listarFaixa(long from, long to) {
        // A lista 'pares' é agora do tipo KeyPointer, então não precisa de cast
        var pares = index.range(from, to);

        var lista = new java.util.ArrayList<domain.Aluno>();

        // Agora 'kp' é garantidamente um KeyPointer
        for (KeyPointer kp : pares) {
            repo.findById(kp.pointerId()).ifPresent(lista::add);
        }

        return lista;
    }


    public boolean removerPorMatricula(long m) {
        var ptr = index.search(m);
        if (ptr.isEmpty()) return false;

        boolean ok = index.remove(m);
        if (!ok) return false;

        // remoção lógica no banco
        repo.softDelete(ptr.get());
        return true;
    }

    public void salvarIndiceCSV(String path) {
        try (var w = java.nio.file.Files.newBufferedWriter(java.nio.file.Path.of(path))) {
            // cabeçalho opcional
            w.write("matricula,id");
            w.newLine();
            for (var kp : index.entriesInOrder()) {
                w.write(kp.matricula() + "," + kp.pointerId());
                w.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao salvar índice: " + e.getMessage(), e);
        }
    }

    public void carregarIndiceCSV(String path) {
        try (var r = java.nio.file.Files.newBufferedReader(java.nio.file.Path.of(path))) {
            String line = r.readLine();
            index.clear();
            while ((line = r.readLine()) != null) { // pula cabeçalho
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",");
                long m = Long.parseLong(parts[0].trim());
                long id = Long.parseLong(parts[1].trim());
                index.insert(m, id);
            }
        } catch (Exception e) {
            throw new RuntimeException("Falha ao carregar índice: " + e.getMessage(), e);
        }
    }

    public int carregarScriptSQL(String path, boolean limparAntes) {
        try (var c = storage.DbBootstrap.getConnection();
             var st = c.createStatement()) {
            c.setAutoCommit(false);
            if (limparAntes) {
                // zera a tabela antes de importar o script
                st.executeUpdate("DELETE FROM aluno");
            }
            c.commit();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao limpar tabela: " + e.getMessage(), e);
        }

        try {
            storage.DbUtils.runSqlScriptFromClasspath(path); // insere tudo do arquivo
        } catch (Exception e) {
            throw new RuntimeException("Falha ao executar script: " + e.getMessage(), e);
        }

        // Reconstroi índice a partir do DB novo
        reconstruirIndice();
        // Retorna quantidade de chaves na árvore (se tiver size())
        try {
            var m = BTree.class.getMethod("size");
            return (int) m.invoke(index);
        } catch (Exception ignore) {
            return 0;
        }
    }



    public String visualizarArvoreAscii() { return index.printAscii(); }


    public Optional<Aluno> buscarPorId(long id) { return repo.findById(id); }
    public String visualizarArvoreInOrder() { return index.printInOrder(); }
}
