# Futebol de Cegos Manager (Android)

App de gerenciamento de futebol de cegos pro Android — estilo
Brasfoot, mas **código 100% original**, escrito do zero (não usa nem
se baseia em nenhum código decompilado de terceiros).

## ⚠️ O que foi rigorosamente testado, e o que não foi

**`Motor.kt`, `Competicao.kt` e `Mercado.kt`** — compilei com o Kotlin
1.9.24 de verdade (baixei a versão certa depois que a do `apt` deu
falso alarme por ser de 2019) e **rodei a lógica de verdade**, não só
compilei: 35 testes formais em JUnit (`MotorTest.kt`,
`CompeticaoTest.kt`, `MercadoTest.kt`), todos espelhando execução real
que já confirmei passando via executáveis separados antes de escrever
a versão JUnit final.

**`MainActivity.kt`** — usa Jetpack Compose, que precisa do SDK do
Android completo pra compilar (não tenho isso no meu ambiente). Não
consegui compilar nem rodar esse arquivo - só revisei o código com
cuidado. **Precisa ser você testando no Android Studio, ou pelo
GitHub Actions**, pra confirmar que a tela abre certinho.

## 🤖 Compilação automática pelo GitHub Actions

Já deixei o workflow pronto (`.github/workflows/compilar.yml`) - a
cada push pro repositório (ou disparando manualmente pela aba
"Actions"), ele:
1. Roda todos os 35 testes JUnit
2. Compila o APK de debug
3. Disponibiliza o APK e o relatório de testes pra download, na
   própria aba "Actions" do GitHub

Também adicionei os arquivos do Gradle Wrapper (`gradlew`, etc.) que
faltavam - peguei de um repositório oficial do Google
(`android/architecture-samples`) pra garantir que é uma versão
confiável (Gradle 8.11.1).

## 💰 Mercado de transferências

- Valor de mercado cresce de forma **não-linear** com o overall do
  jogador (um craque vale muito mais que vários jogadores medianos
  somados, igual no futebol de verdade)
- Cada time tem um orçamento (`orcamento`, em reais fictícios)
- Times recusam oferta abaixo do valor justo + uma margem de 15%
- Times recusam vender o **último** jogador de uma posição (nunca
  fica sem goleiro, por exemplo)
- Testado com 6 cenários: oferta boa (aceita), oferta baixa (recusada), comprador sem dinheiro (recusado), proteção do último goleiro, e ordenação do elenco por valor

**Ainda falta**: uma tela (Compose) pra usar isso de verdade - hoje só
existe a lógica testada, sem interface ainda.

## Times reais, elencos com craques reais

Pesquisei a tabela oficial do **Campeonato Brasileiro Série A 2025**
(CBDV) — os 12 times são reais, incluindo os grupos e os resultados de
verdade das quartas/semis/final, usados pra calcular a força de cada
time:

| Nível | Times | Base real |
|---|---|---|
| 5 (mais forte) | AGAFUC-RS, Corinthians-SP | Campeão e vice 2025 (final foi nos pênaltis) |
| 4 | APACE-PB, AMC-MT | 3º e 4º lugar 2025 |
| 3 | INV-SP, APADV-SP, ADESUL-CE | Eliminados nas quartas, por margem pequena |
| 2 | APADEVI-PB | Eliminado nas quartas, por margem grande |
| 1 (mais fraco) | UNIACE-DF, INSEP-SP, CEDEMAC-MA, Vila Nova-GO | Não passaram da fase de grupos |

**Jogadores reais confirmados**: Ricardinho e Nonato (AGAFUC - Seleção
Brasileira, bronze em Paris 2024), Tiago Paraná, Cássio e Jefinho
(Corinthians - Seleção também), Paulinho (APACE), goleiros Luan
(AGAFUC) e Giovanni (Corinthians). O resto de cada elenco é fictício.

Grupos reais:
- **Grupo A**: AGAFUC, ADESUL, UNIACE, INSEP
- **Grupo B**: Corinthians, INV, APADV, CEDEMAC
- **Grupo C**: APACE, APADEVI, AMC, Vila Nova

## O que já está pronto

- Simulação de partida narrada minuto a minuto (estilo Brasfoot)
- Escalação automática (melhores jogadores por posição)
- Fase de grupos completa (todos contra todos dentro de cada grupo)
- Tabela de classificação por grupo
- Artilharia
- Tela inicial (Compose) mostrando a tabela e um botão pra simular a
  fase de grupos - **não testada, ver aviso acima**

## Próximos passos (ainda não construídos)

- Tela do mercado de transferências (a lógica já está pronta e testada)
- Escalação manual (hoje é só automática)
- Fase eliminatória (quartas, semis, final) depois da fase de grupos
- Persistência (salvar o progresso do campeonato)
- Elenco completo dos outros 9 times (hoje só AGAFUC, Corinthians e
  APACE têm jogadores reais confirmados)

## Como usar o GitHub Actions (recomendado, você já usa esse fluxo)

1. Cria um repositório novo no GitHub (ou usa um existente)
2. Sobe essa pasta inteira pra lá (`git add . && git commit -m "primeira versão" && git push`)
3. Vai na aba "Actions" do repositório - o workflow "Compilar App" já
   deve estar rodando sozinho (ou clica em "Run workflow" pra disparar
   na mão)
4. Espera terminar, desce até "Artifacts" no final da execução, baixa
   o `app-debug-apk`

## Como abrir no Android Studio (alternativa)

1. Abre o Android Studio
2. "Open" -> seleciona a pasta `FutCegosManager`
3. Deixa o Gradle sincronizar (primeira vez demora um pouco)
4. Roda os testes: botão direito em `app/src/test` -> "Run Tests"
5. Roda o app num emulador ou celular conectado

## Estrutura

```
FutCegosManager/
├── .github/workflows/compilar.yml    # GitHub Actions - testa e compila sozinho
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/   # Gradle Wrapper (peguei de repo oficial do Google)
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/                     # ícone adaptativo, strings
│       │   └── java/com/martim/futcegosmanager/
│       │       ├── Motor.kt          # TESTADO (compilado + executado)
│       │       ├── Competicao.kt      # TESTADO (compilado + executado)
│       │       ├── Mercado.kt           # TESTADO (compilado + executado)
│       │       └── MainActivity.kt      # NÃO testado (precisa do SDK Android)
│       └── test/
│           └── java/com/martim/futcegosmanager/
│               ├── MotorTest.kt
│               ├── CompeticaoTest.kt
│               └── MercadoTest.kt
```
