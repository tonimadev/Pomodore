<img src="/images/pomodore-banner.png" width="1024" height="500">

# Pomodore - App Pomodoro com MVVM

App Android de técnica Pomodoro desenvolvido com arquitetura MVVM, Jetpack Compose e internacionalização completa.

## Download

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://playbadges.pavi2410.com/badge/full?id=digital.tonima.pomodore&theme=dark">
  <img alt="PlayBadges Card Folo" src="https://playbadges.pavi2410.com/badge/full?id=digital.tonima.pomodore">
</picture>

<a href='https://play.google.com/store/apps/details?id=digital.tonima.pomodore' target="_blank" rel="noopener noreferrer"><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width='200'/></a>


## ✨ Funcionalidades

- ⏱️ **Timer Pomodoro**: 25 min trabalho, 5 min pausa curta, 15 min pausa longa
- 🔔 **Notificação Persistente**: Acompanhe o timer sem manter o app aberto
- 🌍 **11 Idiomas**: Inglês, Espanhol, Chinês Simplificado, Hindi, Árabe, Bengali, Português (BR e PT), Russo, Japonês e Francês
- 🎨 **Material Design 3**: Interface moderna com Jetpack Compose
- 💾 **Persistência**: Configurações salvas com DataStore

## 🏗️ Arquitetura

O projeto segue a arquitetura **MVVM (Model-View-ViewModel)** com as seguintes camadas:

```
app/src/main/java/digital/tonima/pomodore/
├── data/
│   ├── model/              # Modelos de dados
│   │   ├── TimerMode.kt
│   │   ├── TimerState.kt
│   │   ├── PomodoroSettings.kt
│   │   └── PomodoroUiState.kt
│   └── repository/         # Camada de dados
│       └── PomodoroRepository.kt
├── ui/
│   ├── components/         # Componentes reutilizáveis
│   │   ├── CircularProgressTimer.kt
│   │   ├── TimerDisplay.kt
│   │   ├── PomodoroButton.kt
│   │   └── SessionCounter.kt
│   ├── screens/            # Telas do app
│   │   └── PomodoroScreen.kt
│   ├── viewmodel/          # ViewModels
│   │   └── PomodoroViewModel.kt
│   └── theme/              # Temas e cores
├── service/                # Serviços
│   └── PomodoroForegroundService.kt
├── util/                   # Utilitários
│   └── Constants.kt
└── MainActivity.kt
```

## 🔧 Tecnologias Utilizadas

- **Kotlin** - Linguagem de programação
- **Jetpack Compose** - UI moderna e declarativa
- **Hilt** - Injeção de dependências
- **ViewModel & StateFlow** - Gerenciamento de estado
- **DataStore** - Persistência de configurações
- **Foreground Service** - Notificações persistentes
- **Material Design 3** - Design system
- **Coroutines & Flow** - Programação assíncrona

## 🌍 Internacionalização

Todas as strings estão no `strings.xml` e traduzidas para:

- 🇺🇸 Inglês (default)
- 🇪🇸 Espanhol
- 🇨🇳 Chinês Simplificado
- 🇮🇳 Hindi
- 🇸🇦 Árabe
- 🇧🇩 Bengali
- 🇧🇷 Português (Brasil)
- 🇵🇹 Português (Portugal)
- 🇷🇺 Russo
- 🇯🇵 Japonês
- 🇫🇷 Francês

## 📱 Funcionalidades do Timer

### Estados do Timer
- **Idle**: Timer parado
- **Running**: Timer em execução
- **Paused**: Timer pausado
- **Completed**: Timer completado

### Ações Disponíveis
- **Start**: Iniciar timer
- **Pause**: Pausar timer
- **Resume**: Retomar timer
- **Stop**: Parar e resetar timer
- **Skip**: Pular para próxima sessão

### Ciclo Pomodoro
1. 25 minutos de trabalho
2. 5 minutos de pausa curta
3. Após 4 sessões: 15 minutos de pausa longa

## 🔔 Notificações

O app usa um **Foreground Service** para manter uma notificação persistente que:
- Mostra o tempo restante
- Exibe o tipo de sessão (Trabalho/Pausa)
- Permite pausar, retomar e pular pelo notificação
- Funciona mesmo com o app em segundo plano

## 🎨 Componentes Compose Reutilizáveis

### CircularProgressTimer
Indicador circular de progresso do timer

### TimerDisplay
Display do tempo restante em formato MM:SS

### PomodoroButton
Botão estilizado (primário e secundário)

### SessionCounter
Contador de sessões completadas

## 💉 Injeção de Dependências com Hilt

O projeto usa **Hilt** para injeção de dependências:

### Módulos
- **AppModule**: Provê o `PomodoroRepository` como singleton

### ViewModels
- `PomodoroViewModel` anotado com `@HiltViewModel` e recebe `PomodoroRepository` via construtor

### Application
- `PomodoroApplication` anotada com `@HiltAndroidApp`

### Activities
- `MainActivity` anotada com `@AndroidEntryPoint` para suportar injeção

## 📝 Permissões

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />
```

## 🚀 Como Usar

1. Abra o app
2. Toque em "Iniciar" para começar uma sessão de trabalho
3. O timer contará 25 minutos
4. Acompanhe pelo notificação se quiser
5. Quando completar, automaticamente inicia a pausa
6. Use "Pular" para avançar para próxima sessão
7. Use "Pausar" para pausar temporariamente

## 🔮 Próximas Melhorias

- [ ] Tela de configurações para personalizar durações
- [ ] Histórico de sessões com Room Database
- [ ] Estatísticas e gráficos de produtividade
- [ ] Sons e vibração ao completar
- [ ] Tema claro/escuro
- [ ] Widgets para tela inicial

## 📄 Licença

Este projeto foi criado como base de app Pomodoro com arquitetura MVVM.

