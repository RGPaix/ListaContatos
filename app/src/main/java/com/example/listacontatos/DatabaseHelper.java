package com.example.listacontatos;

//Imports
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.listacontatos.model.Contato;

import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseHelper extends SQLiteOpenHelper {

    //Atributos do Banco de Dados
    private static final String DATABASE_NAME = "lista_de_contatos.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "contatos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOME = "nome";
    private static final String COLUMN_TELEFONE = "telefone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_LINKEDIN = "linkedin";
    private static final String COLUMN_FAVORITO = "favorito";

    //Criando Banco de Dados
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Criando tabela e colunas
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOME + " TEXT, " +
                COLUMN_TELEFONE + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_LINKEDIN + " TEXT, " +
                COLUMN_FAVORITO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    //Dropar tabela se ela já existir
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Metodo Adicionar Contato
    public long adicionarContato(Contato contato) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, contato.getNome());
        values.put(COLUMN_TELEFONE, contato.getTelefone());
        values.put(COLUMN_EMAIL, contato.getEmail());
        values.put(COLUMN_LINKEDIN, contato.getLinkedin());
        values.put(COLUMN_FAVORITO, contato.isFavorito() ? 1 : 0);

        // Inserir linha na tabela
        long id = db.insert(TABLE_NAME, null, values);
        // Fechar conexão com o banco
        db.close();
        // Retorna o ID do novo registro no banco
        return id;
    }

    // Metodo para atualizar contato
    public boolean atualizarContato(Contato contato) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("nome", contato.getNome());
        values.put("telefone", contato.getTelefone());
        values.put("email", contato.getEmail());
        values.put("linkedin", contato.getLinkedin());
        values.put("favorito", contato.isFavorito() ? 1 : 0);

        int rowsAffected = db.update("contatos", values, "id = ?", new String[]{
                String.valueOf(contato.getId())
        });
        db.close();
        return rowsAffected > 0;
    }

    //Metodo Obter todos os contatos
    public ArrayList<Contato> obterTodosContatos() {
        ArrayList<Contato> contatos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // DEBUG: Verifique os nomes das colunas disponíveis
        String[] columnNames = cursor.getColumnNames();
        Log.d("DB_DEBUG", "Colunas disponíveis: " + Arrays.toString(columnNames));

        if (cursor.moveToFirst()) {
            do {
                Contato contato = new Contato();

                // Acesso pelos índices numéricos (mais seguro)
                contato.setId(cursor.getLong(0));
                contato.setNome(cursor.getString(1));
                contato.setTelefone(cursor.getString(2));
                contato.setEmail(cursor.getString(3));
                contato.setLinkedin(cursor.getString(4));

                // Para acessar a coluna favorito (a 6ª coluna)
                if (cursor.getColumnCount() > 5) {
                    contato.setFavorito(cursor.getInt(5) == 1);
                }

                contatos.add(contato);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contatos;
    }

    //Metodo remover um contato
    public boolean removerContato(Contato contato) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Usa o ID como critério (mais seguro que nome)
            int result = db.delete(
                    TABLE_NAME,
                    COLUMN_ID + " = ?",  // WHERE clause
                    new String[]{String.valueOf(contato.getId())}  // Valor para o WHERE
            );

            return result > 0;

        } catch (SQLException e) {
            Log.e("DB_ERROR", "Erro ao remover contato", e);
            return false;
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    //Metodo para remover todos os favoritos
    public void removerTodosFavoritos() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("favorito", 0);
        db.update("contatos", values, null, null);
    }
}