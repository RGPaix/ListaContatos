package com.example.listacontatos;

//Imports
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.listacontatos.model.Contato;

public class DetalhesActivity extends AppCompatActivity {

    //Atributos dos elementos visuais
    private EditText editNome, editTelefone, editEmail, editLinkedin;
    private ImageButton btnLigar, btnEmail, btnLinkedin, btnFavorito;
    private Button btnEditar;
    private Button btnVoltar;
    private Contato contato;

    //Atributo do Banco de Dados
    private DatabaseHelper dbHelper;

    //Atributos para manipular dados
    private boolean isEditMode = false;
    private boolean isFavorito = false;

    //Metodo onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes);

        // Inicializa as views
        initViews();

        // Obtém o contato passado pela MainActivity
        contato = (Contato) getIntent().getSerializableExtra("contato");
        dbHelper = new DatabaseHelper(this);

        // Preenche os campos
        preencherDados();

        // Configura listeners
        setupListeners();
    }

    //Metodo para buscar os elementos visuais no XML
    private void initViews() {
        editNome = findViewById(R.id.editNome);
        editTelefone = findViewById(R.id.editTelefone);
        editEmail = findViewById(R.id.editEmail);
        editLinkedin = findViewById(R.id.editLinkedin);

        btnLigar = findViewById(R.id.btnLigar);
        btnEmail = findViewById(R.id.btnEmail);
        btnLinkedin = findViewById(R.id.btnLinkedin);
        btnFavorito = findViewById(R.id.btnFavorito);
        btnEditar = findViewById(R.id.btnEditar);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    //Metodo para preencher os dados
    private void preencherDados() {
        if (contato != null) {
            editNome.setText(contato.getNome());
            editTelefone.setText(contato.getTelefone());
            editEmail.setText(contato.getEmail());
            editLinkedin.setText(contato.getLinkedin());

            // Verifica se é favorito
            isFavorito = contato.isFavorito();
            atualizarIconeFavorito();
        }
    }

    //Metodo para definir as ações dos botões
    private void setupListeners() {
        // Botão Ligar
        btnLigar.setOnClickListener(v -> {
            if (contato.getTelefone() != null && !contato.getTelefone().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contato.getTelefone()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Número indisponível", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão Email
        btnEmail.setOnClickListener(v -> {
            if (contato.getEmail() != null && !contato.getEmail().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + contato.getEmail()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "E-mail indisponível", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão LinkedIn
        btnLinkedin.setOnClickListener(v -> {
            if (contato.getLinkedin() != null && !contato.getLinkedin().isEmpty()) {
                String url = contato.getLinkedin();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } else {
                Toast.makeText(this, "LinkedIn indisponível", Toast.LENGTH_SHORT).show();
            }
        });

        // Botão Favorito
        btnFavorito.setOnClickListener(v -> {
            if (contato == null) {
                Toast.makeText(this, "Erro ao favoritar: contato inválido.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contato.isFavorito()) {
                // Desmarca todos os outros
                dbHelper.removerTodosFavoritos();
                contato.setFavorito(true);
                isFavorito = true;
            } else {
                contato.setFavorito(false);
                isFavorito = false;
            }

            dbHelper.atualizarContato(contato);
            atualizarIconeFavorito();

            // Atualiza MainActivity com o novo estado (favoritado) do contato
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contatoAtualizado", contato);
            setResult(RESULT_OK, resultIntent);
        });

        // Botão Editar/Salvar
        btnEditar.setOnClickListener(v -> {
            if (isEditMode) {
                // Modo Salvar
                salvarAlteracoes();
            } else {
                // Modo Editar
                entrarModoEdicao();
            }
        });

        // Botão Voltar
        btnVoltar.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("contatoAtualizado", contato);
            resultIntent.putExtra("position", getIntent().getIntExtra("position", -1));
            setResult(RESULT_OK, resultIntent);
            finish(); // Retorna para a MainActivity
        });
    }

    //Metodo para definir cada modo do botão Editar/Salvar
    private void entrarModoEdicao() {
        isEditMode = true;
        btnEditar.setText("Salvar");

        // Habilita edição
        editNome.setEnabled(true);
        editTelefone.setEnabled(true);
        editEmail.setEnabled(true);
        editLinkedin.setEnabled(true);

        // Foca no primeiro campo
        editNome.requestFocus();
    }

    private void salvarAlteracoes() {
        isEditMode = false;
        btnEditar.setText("Editar");

        // Desabilita edição
        editNome.setEnabled(false);
        editTelefone.setEnabled(false);
        editEmail.setEnabled(false);
        editLinkedin.setEnabled(false);

        // Atualiza o objeto contato com os novos dados
        String novoNome = editNome.getText().toString().trim();
        String novoTelefone = editTelefone.getText().toString().trim();
        String novoEmail = editEmail.getText().toString().trim();
        String novoLinkedin = editLinkedin.getText().toString().trim();

        if (!novoNome.isEmpty()) {
            contato.setNome(novoNome);
            contato.setTelefone(novoTelefone);
            contato.setEmail(novoEmail);
            contato.setLinkedin(novoLinkedin);

            dbHelper.atualizarContato(contato);

            Toast.makeText(this, "Contato atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "O nome é obrigatório.", Toast.LENGTH_SHORT).show();
        }
    }

    //Atualizar icone de favorito
    private void atualizarIconeFavorito() {
        if (contato != null && contato.isFavorito()) {
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
}
