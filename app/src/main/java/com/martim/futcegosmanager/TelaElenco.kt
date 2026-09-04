package com.martim.futcegosmanager

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * TelaElenco.kt
 *
 * Tela do elenco do time - mostra os jogadores agrupados (goleiros
 * primeiro, depois linha), com overall, valor de mercado e estado
 * físico. Código original, inspirado só no CONCEITO de layout comum
 * a esse gênero de jogo (lista de jogadores com atributos) - não usa
 * nada do código decompilado do Brasfoot.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaElenco(
    time: Time,
    onVoltar: () -> Unit,
    onSelecionarJogador: (Jogador) -> Unit,
    onIrParaEscalacao: () -> Unit = {},
    onIrParaTaticas: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${time.nome}-${time.sigla}") },
                navigationIcon = {
                    IconButton(
                        onClick = onVoltar,
                        modifier = Modifier.semantics { contentDescription = "Voltar" },
                    ) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                },
            )
        },
        bottomBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onIrParaEscalacao,
                    modifier = Modifier.weight(1f).semantics { contentDescription = "Ir pra tela de escalação" },
                ) { Text("Escalação") }
                OutlinedButton(
                    onClick = onIrParaTaticas,
                    modifier = Modifier.weight(1f).semantics { contentDescription = "Ir pra tela de táticas" },
                ) { Text("Táticas") }
            }
        },
    ) { paddingInterno ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
            CartaoResumoTime(time)

            val goleiros = time.elenco.filter { it.posicao == Posicao.GOLEIRO }.sortedByDescending { it.overall() }
            val linha = time.elenco.filter { it.posicao == Posicao.LINHA }.sortedByDescending { it.overall() }

            LazyColumn(modifier = Modifier.weight(1f)) {
                item { CabecalhoDeGrupo("Goleiros") }
                items(goleiros) { jogador -> LinhaJogador(jogador, time, onSelecionarJogador) }
                item { CabecalhoDeGrupo("Linha") }
                items(linha) { jogador -> LinhaJogador(jogador, time, onSelecionarJogador) }
            }
        }
    }
}

@Composable
private fun CartaoResumoTime(time: Time) {
    Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Orçamento: R$ ${formatarValor(time.orcamento)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { contentDescription = "Orçamento do time: ${formatarValor(time.orcamento)} reais" },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Força geral: ${time.forcaGeral().toInt()}",
                modifier = Modifier.semantics { contentDescription = "Força geral do time: ${time.forcaGeral().toInt()}" },
            )
            Spacer(Modifier.height(4.dp))
            Text("${time.pontos} pts | V:${time.vitorias} E:${time.empates} D:${time.derrotas}")
        }
    }
}

@Composable
private fun CabecalhoDeGrupo(titulo: String) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun LinhaJogador(jogador: Jogador, time: Time, onSelecionar: (Jogador) -> Unit) {
    val titular = time.escalacaoTitular().contains(jogador)
    val descricao = "${jogador.nome}, overall ${jogador.overall()}, " +
        "${if (titular) "titular" else "reserva"}, valor de mercado ${formatarValor(valorDeMercado(jogador))} reais, " +
        "${jogador.golsNaTemporada} gols na temporada"

    ListItem(
        headlineContent = { Text(jogador.nome) },
        supportingContent = {
            Text(
                (if (jogador.posicao == Posicao.GOLEIRO) "Reflexo ${jogador.reflexo}" else "Ataque ${jogador.ataque}") +
                    " | Defesa ${jogador.defesa} | Físico ${jogador.fisico}"
            )
        },
        leadingContent = {
            if (titular) Icon(Icons.Filled.Star, contentDescription = null)
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(jogador.overall().toString(), style = MaterialTheme.typography.titleMedium)
                Text("R$ ${formatarValor(valorDeMercado(jogador))}", style = MaterialTheme.typography.bodySmall)
            }
        },
        modifier = Modifier
            .clickable { onSelecionar(jogador) }
            .semantics { contentDescription = descricao },
    )
    HorizontalDivider()
}

fun formatarValor(valor: Long): String = when {
    valor >= 1_000_000 -> "%.1fM".format(valor / 1_000_000.0)
    valor >= 1_000 -> "%.0fk".format(valor / 1_000.0)
    else -> valor.toString()
}
