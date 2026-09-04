package com.martim.futcegosmanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * TelaTaticas.kt
 *
 * Tela de táticas - quem bate falta/escanteio, capitão, estilo de
 * jogo, intensidade de marcação, foco de ataque. Código original.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaTaticas(time: Time, taticas: Taticas, onVoltar: () -> Unit, onSalvar: (Taticas) -> Unit) {
    var batedorFaltas by remember { mutableStateOf(taticas.batedorFaltas) }
    var capitao by remember { mutableStateOf(taticas.capitao) }
    var batedorEscanteios by remember { mutableStateOf(taticas.batedorEscanteios) }
    var estilo by remember { mutableStateOf(taticas.estiloDeJogo) }
    var marcacao by remember { mutableStateOf(taticas.marcacao) }
    var foco by remember { mutableStateOf(taticas.focoDeAtaque) }

    val jogadoresDeLinha = time.elenco.filter { it.posicao == Posicao.LINHA }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Táticas") },
                navigationIcon = {
                    IconButton(onClick = onVoltar, modifier = Modifier.semantics { contentDescription = "Voltar" }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    taticas.batedorFaltas = batedorFaltas
                    taticas.capitao = capitao
                    taticas.batedorEscanteios = batedorEscanteios
                    taticas.estiloDeJogo = estilo
                    taticas.marcacao = marcacao
                    taticas.focoDeAtaque = foco
                    onSalvar(taticas)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).semantics { contentDescription = "Salvar táticas" },
            ) { Text("Salvar") }
        },
    ) { paddingInterno ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingInterno).padding(16.dp)) {
            SeletorDeJogador("Batedor de faltas", batedorFaltas, jogadoresDeLinha) { batedorFaltas = it }
            Spacer(Modifier.height(16.dp))
            SeletorDeJogador("Capitão", capitao, time.elenco) { capitao = it }
            Spacer(Modifier.height(16.dp))
            SeletorDeJogador("Batedor de escanteios", batedorEscanteios, jogadoresDeLinha) { batedorEscanteios = it }
            Spacer(Modifier.height(24.dp))

            GrupoDeOpcoes(
                titulo = "Estilo de jogo",
                opcoes = EstiloDeJogo.entries.map { it to nomeEstilo(it) },
                selecionado = estilo,
                onSelecionar = { estilo = it },
            )
            Spacer(Modifier.height(16.dp))
            GrupoDeOpcoes(
                titulo = "Marcação",
                opcoes = IntensidadeMarcacao.entries.map { it to nomeMarcacao(it) },
                selecionado = marcacao,
                onSelecionar = { marcacao = it },
            )
            Spacer(Modifier.height(16.dp))
            GrupoDeOpcoes(
                titulo = "Concentrar ataques",
                opcoes = FocoDeAtaque.entries.map { it to nomeFoco(it) },
                selecionado = foco,
                onSelecionar = { foco = it },
            )
        }
    }
}

@Composable
private fun SeletorDeJogador(titulo: String, selecionado: Jogador?, opcoes: List<Jogador>, onSelecionar: (Jogador) -> Unit) {
    var expandido by remember { mutableStateOf(false) }
    Text(titulo, style = MaterialTheme.typography.labelLarge)
    Spacer(Modifier.height(4.dp))
    Box {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$titulo: ${selecionado?.nome ?: "não definido"}. Toque pra escolher." },
        ) {
            Text(selecionado?.nome ?: "Não definido")
        }
        DropdownMenu(expanded = expandido, onDismissRequest = { expandido = false }) {
            opcoes.forEach { jogador ->
                DropdownMenuItem(text = { Text(jogador.nome) }, onClick = { onSelecionar(jogador); expandido = false })
            }
        }
    }
}

@Composable
private fun <T> GrupoDeOpcoes(titulo: String, opcoes: List<Pair<T, String>>, selecionado: T, onSelecionar: (T) -> Unit) {
    Text(titulo, style = MaterialTheme.typography.labelLarge)
    Column {
        opcoes.forEach { (valor, nome) ->
            val estaSelecionado = valor == selecionado
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = estaSelecionado, onClick = { onSelecionar(valor) }, role = Role.RadioButton)
                    .semantics { contentDescription = "$nome${if (estaSelecionado) ", selecionado" else ""}" },
            ) {
                RadioButton(selected = estaSelecionado, onClick = { onSelecionar(valor) })
                Text(nome)
            }
        }
    }
}

private fun nomeEstilo(e: EstiloDeJogo) = when (e) {
    EstiloDeJogo.EQUILIBRADO -> "Equilibrado"
    EstiloDeJogo.ATAQUE_TOTAL -> "Ataque Total"
    EstiloDeJogo.CONTRA_ATAQUE -> "Contra-ataque"
}

private fun nomeMarcacao(m: IntensidadeMarcacao) = when (m) {
    IntensidadeMarcacao.LEVE -> "Leve"
    IntensidadeMarcacao.PESADA -> "Pesada"
    IntensidadeMarcacao.MUITO_PESADA -> "Muito Pesada"
}

private fun nomeFoco(f: FocoDeAtaque) = when (f) {
    FocoDeAtaque.PELO_MEIO -> "Pelo meio"
    FocoDeAtaque.PELAS_LATERAIS -> "Pelas laterais"
}
