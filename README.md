📚 Sistema de Indexação de Alunos com B-Tree

Projeto acadêmico que aplica a estrutura de dados B-Tree como índice para um sistema de cadastro e consulta de alunos, com foco em busca eficiente, remoção, inserção e listagem por faixa.

📌 Descrição

Este projeto consiste no desenvolvimento de um sistema de cadastro e consulta de alunos, utilizando uma B-Tree como estrutura de indexação para otimizar operações de busca, inserção, remoção e listagem por faixa de matrículas.

O trabalho foi desenvolvido como parte da disciplina de Estruturas de Dados, com o objetivo de aplicar, de forma prática, os conceitos estudados sobre árvores balanceadas, demonstrando sua empregabilidade em um cenário próximo ao de sistemas reais, como os utilizados em bancos de dados.

🎯 Objetivos

Aplicar a estrutura de dados B-Tree em um sistema funcional

Utilizar a matrícula como chave de indexação

Implementar operações fundamentais da B-Tree:

Inserção

Busca

Remoção

Listagem por faixa

Integrar a B-Tree a um banco de dados

Implementar persistência do índice

Demonstrar separação entre dados e estrutura de indexação

🧠 Estrutura de Dados Utilizada
B-Tree

A B-Tree é uma estrutura de dados balanceada, amplamente utilizada como índice em sistemas de banco de dados. Ela mantém as chaves ordenadas e garante desempenho consistente para operações de busca, inserção e remoção.

No projeto:

A matrícula é utilizada como chave

A B-Tree armazena pares (matrícula, ponteiro)

O ponteiro referencia o registro do aluno no banco de dados

Todas as operações respeitam o grau mínimo t, garantindo o balanceamento da árvore

🏗️ Arquitetura da Solução

O sistema foi projetado com separação clara de responsabilidades:

Banco de Dados

Armazena os dados completos dos alunos

Atua como fonte de verdade

B-Tree

Funciona como índice em memória

Armazena apenas a matrícula e o ponteiro para o banco

Arquivo CSV

Utilizado para persistir o índice da B-Tree

Permite salvar e recarregar a estrutura sem reconstrução total

Fluxo geral:

Banco de Dados ⇄ B-Tree ⇄ Arquivo CSV

⚙️ Funcionalidades Implementadas

O sistema possui um menu interativo via terminal com as seguintes opções:

Cadastrar aluno

Buscar aluno por matrícula

Remover aluno por matrícula

Listar alunos por faixa de matrícula

Exportar a B-Tree para arquivo

Visualizar a B-Tree (percurso em ordem)

Visualizar a B-Tree em formato hierárquico (Pretty Print)

Salvar o índice da B-Tree em arquivo CSV

Carregar o índice da B-Tree a partir de um CSV

Reconstruir o índice a partir do banco de dados

Repopular o banco de dados a partir de scripts SQL

🔍 Busca por Faixa

A listagem por faixa é realizada utilizando um percurso em ordem (in-order) na B-Tree, explorando a ordenação natural da estrutura. Apenas as matrículas dentro do intervalo informado são retornadas, evitando varreduras completas no banco de dados.

As chaves internas da B-Tree atuam como separadores estruturais, enquanto os registros efetivos são obtidos a partir das folhas da árvore.

💾 Persistência do Índice

O índice da B-Tree pode ser:

Salvo em um arquivo CSV

Recarregado a partir do CSV

Reconstruído diretamente a partir do banco de dados

Essa abordagem garante consistência entre dados e índice e aproxima o projeto do funcionamento de sistemas reais de informação.

▶️ Como Executar

Clone o repositório

Configure o banco de dados utilizado pelo projeto

Compile o projeto em Java

Execute a aplicação via terminal

Utilize o menu interativo para acessar as funcionalidades

🧪 Tecnologias Utilizadas

Linguagem: Java

Banco de Dados: SQL

Estrutura de Dados: B-Tree

Persistência adicional: Arquivo CSV

Interface: Terminal (CLI)
