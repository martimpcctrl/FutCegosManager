package com.martim.futcegosmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                TelaPrincipal()
            }
        }
    }
}

@Composable
fun TelaPrincipal() {
    // Estado do campeonato inteiro fica aqui - sem persistência ainda
    // (isso é próximo passo: salvar progresso, mercado de
    // transferências, escalação manual).
    var times by remember { mutableStateOf(montarCampeonatoSerieA(seed = System.currentTimeMillis())) }
    var faseSimulada by remember { mutableStateOf(false) }
    var grupoSelecionado by remember { mutableStateOf("A") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Campeonato Brasileiro de Futebol de Cegos - Série A",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { contentDescription = "Campeonato Brasileiro de Futebol de Cegos, Série A" },
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("A", "B", "C").forEach { grupo ->
                FilterChip(
                    selected = grupoSelecionado == grupo,
                    onClick = { grupoSelecionado = grupo },
                    label = { Text("Grupo $grupo") },
                    modifier = Modifier.semantics { contentDescription = "Ver grupo $grupo" },
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                jogarFaseDeGrupos(times, seed = System.currentTimeMillis())
                faseSimulada = true
            },
            modifier = Modifier.semantics { contentDescription = "Simular todos os jogos da fase de grupos" },
        ) {
            Text(if (faseSimulada) "Simular de novo" else "Simular fase de grupos")
        }
        Spacer(Modifier.height(16.dp))

        val tabela = tabelaDoGrupo(times, grupoSelecionado)
        Text("Classificação - Grupo $grupoSelecionado", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            itemsIndexed(tabela) { indice, time ->
                val descricao = "${indice + 1}º lugar: ${time.nome}, ${time.pontos} pontos, " +
                    "${time.vitorias} vitórias, ${time.empates} empates, ${time.derrotas} derrotas, " +
                    "saldo de gols ${time.saldoGols()}"
                ListItem(
                    headlineContent = { Text("${indice + 1}. ${time.nome}-${time.sigla}") },
                    supportingContent = { Text("${time.pontos} pts | V:${time.vitorias} E:${time.empates} D:${time.derrotas} | SG:${time.saldoGols()}") },
                    modifier = Modifier.semantics { contentDescription = descricao },
                )
            }
        }
    }
}
