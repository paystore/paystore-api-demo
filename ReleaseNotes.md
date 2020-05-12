## App Demonstração API Pagamento - Applet Phoebus

### Versão: 1.2.0.0

- 53826
  - .Removido captura do campo getCardToken da funcao callResultIntent que usa como parametro o objeto Payment; a outra funcao sobreposta utiliza o objeto PaymentV2 no qual existe o parametro getCardToken;

- 41073
  - Ajuste no nome do ícone do app
  - removendo toast
  - Criação do menu para seleção das adquirentes.
  - Criação da tela para seleção dos tipos de pagamento.
  - Passando os Tipos de pagamento selecionados para Payments apk.
  - Criação de métodos utilitárioas para ler e escrever preferencias.

- 51842
  - Ajuste no titulo da tela
  - Diversas melhorias de usabilidade no appDemo
  - removendo activitys não utilizadas mais
  - Diversas melhorias de usabilidade no appDemo
  - Criação de classe com funcionalidades de papagamento.
  - ajuste na mensagem

- 51852
  - Substituir PaymentRequest -> PaymentRequestV2 no tipo do parametro
  - Renomear pr -> paymentRequestV2 pra facilitar a leitura
  - Separar Helper por finalidade (AlertUtils e LogUtils)
  - Mover constantes de extras em Helper para MainActivity
  - Utilizar metodo que formata data já existente
  - Remover imports não utilizados
  - Padronizar quebras de linha
  - Ajustar identação     
  - criando nova funcionalidade Pagamento de ponta a ponta, que paga e confirma a transação.
  - refatoração
  - atualizando o aar.
  - criando nova funcionalidade Pagamento de ponta a ponta, que paga e confirma a transação.
  - refatoração

- 51104
  - Remover imports nao utilizados
  - Remover distanciamento do menu das bordas da tela
  - Trocar multiplos ifs por switch
  - Ajustar imagem do icone: manter proporcoes iguais a logo original
  - Alterando icone do app demo com o icone da phoebus.
  - Atualizando o app demo com o Pagamento V2, melhorando o menu e definição de temas.