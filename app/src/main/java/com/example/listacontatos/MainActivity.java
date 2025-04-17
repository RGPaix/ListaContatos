package com.example.listacontatos;

//Imports
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.listacontatos.model.Contato;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //Atributos dos elementos visuais e listas
    private EditText editNome;
    private EditText editTelefone;
    private EditText editEmail;
    private EditText editLinkedin;
    private ListView listaContatos;
    private Button bttnAdicionar;
    private ArrayList<Contato> contatos;
    private ArrayAdapter<Contato> contatosAdapter;

    //Atributo do Banco de Dados
    private DatabaseHelper dbHelper;

    //Atributos do sensor de proximidade
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isCalling = false;

    //Metodo onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Inicializando o sensor de proximidade
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            Toast.makeText(this, "Sensor de proximidade não disponível", Toast.LENGTH_SHORT).show();
        }

        //Procurando os elementos visuais no XML
        editNome = findViewById(R.id.EditNome);
        editTelefone = findViewById(R.id.EditTelefone);
        editEmail = findViewById(R.id.EditEmail);
        editLinkedin = findViewById(R.id.EditLinkedin);
        bttnAdicionar = findViewById(R.id.BttnAdicionar);
        listaContatos = findViewById(R.id.ListaContatos);

        //Inicializando e carregando o Banco de Dados
        dbHelper = new DatabaseHelper(this);
        contatos = dbHelper.obterTodosContatos();

        //Inserindo os dados do banco no XML item_contato
        contatosAdapter = new ArrayAdapter<Contato>(this,
                R.layout.item_contato,
                R.id.textNome,
                contatos) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Contato contato = contatos.get(position);

                TextView textNome = view.findViewById(R.id.textNome);
                TextView textTelefone = view.findViewById(R.id.textTelefone);

                //Adiciona simbolo de estrela ao nome para indicar um contato favoritado na DetalhesActivity
                String nomeComEstrela = (contato.isFavorito() ? "★ " : "") + contato.getNome();
                textNome.setText(nomeComEstrela);
                textTelefone.setText(contato.getTelefone());

                return view;
            }
        };

        //Setando o adapter
        listaContatos.setAdapter(contatosAdapter);

        //Cria contato ao clicar no botão "Adicionar contato"
        bttnAdicionar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adicionarContato();
            }
        });

        //Vai para a DetalhesActivity com um clique simples
        listaContatos.setOnItemClickListener((parent, view, position, id) -> {
            Contato contatoSelecionado = contatos.get(position);

            Intent intent = new Intent(MainActivity.this, DetalhesActivity.class);
            intent.putExtra("contato", contatoSelecionado);
            intent.putExtra("position", position);
            startActivityForResult(intent, 1);
        });

        //Deleta contato com um clique longo
        listaContatos.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return remove(position);
            }
        });
}

    //Metodo para adicionar contatos chamado no botão "Adicionar contato"
    public void adicionarContato () {
        //Captura dados dos ediTexts
        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String linkedin = editLinkedin.getText().toString().trim();

        //Tratamento de exceções
        if (!nome.isEmpty()) {
            Contato novoContato = new Contato();
            novoContato.setNome(nome);
            novoContato.setTelefone(telefone);
            novoContato.setEmail(email);
            novoContato.setLinkedin(linkedin);

            long id = dbHelper.adicionarContato(novoContato);

            if (id != 0) {
                // Atualiza com o ID gerado
                novoContato.setId(id);
                contatos.add(novoContato);
                contatosAdapter.notifyDataSetChanged();

                // Limpa os campos
                editNome.setText("");
                editTelefone.setText("");
                editEmail.setText("");
                editLinkedin.setText("");

                Toast.makeText(this, "Contato adicionado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao adicionar contato!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Informe pelo menos o nome", Toast.LENGTH_SHORT).show();
        }
    }

    //Metodo para remover um contato com o clique longo
    private boolean remove ( int position){
        Contato contato = contatos.get(position);
        dbHelper.removerContato(contato);
        contatos.remove(position);
        contatosAdapter.notifyDataSetChanged();
        Toast.makeText(getApplicationContext(), "Contato removido!", Toast.LENGTH_LONG).show();
        return true;
    }

    // Metodo para atualizar os contatos quando retornar à MainActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Contato contatoAtualizado = (Contato) data.getSerializableExtra("contatoAtualizado");
            int position = data.getIntExtra("position", -1);

            if (contatoAtualizado != null) {
                // Atualizar contatos
                contatos.clear(); // Limpa a lista atual
                contatos.addAll(dbHelper.obterTodosContatos()); // Recarrega a lista direto do banco
                contatosAdapter.notifyDataSetChanged(); // Atualiza a exibição
            }
        }
    }

    //Metodos para usar o sensor
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0];

            // Verifica se a proximidade é suficiente para acionar
            if (distance < proximitySensor.getMaximumRange() && !isCalling) {
                // Mostra mensagem de proximidade
                Toast.makeText(this, "Aproxime bem da tela para acionar o favorito", Toast.LENGTH_SHORT).show();
                Contato favorito = null;
                for (Contato c : contatos) {
                    if (c.isFavorito()) {
                        favorito = c;
                        break;
                    }
                }

                if (favorito != null && favorito.getTelefone() != null && !favorito.getTelefone().isEmpty()) {
                    isCalling = true; // Evita chamadas repetidas

                    // Cria uma intent implícita para abrir o app de chamadas
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + favorito.getTelefone()));
                    startActivity(intent);
                }
            } else if (distance >= proximitySensor.getMaximumRange()) {
                isCalling = false; // Reseta flag quando o objeto se afasta
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Obrigatório, mas não é chamado porque a precisão é irrelevante para o tipo de leitura do sensor usado
    }

    //Metodo para parar sensor
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}