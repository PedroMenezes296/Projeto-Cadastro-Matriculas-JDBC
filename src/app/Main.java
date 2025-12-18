package app;

import app.CLI;
import app.UseCases;
import index.BTree;
import storage.DbBootstrap;
import storage.H2StudentRepository;

public class Main {
    public static void main(String[] args) throws Exception {
        DbBootstrap.initSchema();
        BTree index = new BTree(3);
        H2StudentRepository repo = new H2StudentRepository();

        UseCases uc = new UseCases(index, repo);
        uc.reconstruirIndice();              // <<--- monta a B-Tree ao iniciar
        new CLI(uc).start();

    }
}
