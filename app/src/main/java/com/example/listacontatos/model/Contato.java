package com.example.listacontatos.model;

import java.io.Serializable;

//A classe deve ser serializable para permitir clicar em um contato em particular
public class Contato implements Serializable {
    //Atributos
    private long id;
    private String nome;
    private String telefone;
    private String email;
    private String linkedin;
    private boolean favorito;

    //Getters e setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    //Metodo para transformar os dados em string para exibir no ListView
    @Override
    public String toString() {
        return nome + " \n " + telefone;
    }

    //Metodo para definir um contato como favorito
    public boolean isFavorito() {
        return favorito;
    }

    public void setFavorito(boolean favorito) {
        this.favorito = favorito;
    }
}