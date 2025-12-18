package domain;

public class Aluno {
    private long id;
    private long matricula;   // <- era int
    private String nome;
    private String curso;
    private String email;
    private boolean ativo = true;

    public Aluno() {}
    public Aluno(long matricula, String nome, String curso, String email) { // <- long
        this.matricula = matricula; this.nome = nome; this.curso = curso; this.email = email;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getMatricula() { return matricula; }           // <- long
    public void setMatricula(long matricula) { this.matricula = matricula; } // <- long
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
