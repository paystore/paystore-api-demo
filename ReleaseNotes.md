## App Demonstração API Pagamento - Applet Phoebus
### Versão: 1.3.0.0 Compatível com SUITE Payments >= 2.6.43.0
- 73361
  - colocando espaçamento no texto.
  - injetando logs na consulta do settlements(fechamento de lote)

- 72151
  - adicionando o request e response logs nos serviços de Settlement.

- 73212
  - ajuste para formatar corretamente qualquer tipo de valor que venha ser inserido.

- 72669 
  - Ajustes na reimpressão

- 72658 
  - Ajuste na validação de data/hora na reimpressão

- 72712 
  - Atualizando aar
  - Adicionando validação nos campos de DNI e notas.

- 72000 
  - Exibindo erro em caso de falha no fechamento de lote

- 72417 
  - Ajustes no filtro de trxType

- 72658 
  - Adicionando validação de data/hora quando incompleta

- 72559
  - removendo dependências não utilizadas

- 72417 
  - Adicionando filtros para consulta de tipos de transações
  - Adicionando filtros para DNI e NOTES no app-demo

- 72787
  - exibir detalhe do pagamento e comprovante, ao clicar no pagamento na tela de listar pagamentos.

- 72116 
  - Adicionando notes e dni no pagamento

- 72414 
  - Ajustes na devolução não referenciada
  - Ajustes na devolução não referenciada via API
  - tela da devolução não referenciada e alterações da tela de consulta

- 71306 
  - Criação de funcionalidade para reimpressão.
  - correção de strings

### Versão: 1.3.0.0 Compatível com SUITE Payments >= 2.6.40.0
- 71380
  - Ajuste para exibir a tela de result após o pagamento com sucesso .
  - .Atualizacao do aar com versao compatível sdk 0206 de payments_debug_2.6.40;
  - .ajuste no MainActivity, removido um bind que jah estava sendo criado;
  - .Ajuste para relaizar a confirmacao quando a escolha do fluxo for pagamento ponta a ponta;
  - adicionando novas adquirentes.
  - Ajuste para considerar o valor do additional value apenas quando o additional type for selecionado.
  - tela do fechamento de lote atualizada
  - adicionando mascara ao edit value.
  - Ajustando o bind na tela principal.
  - Criando tela que mostre a resposta do fechamento de lote
  - Criando tela que mostre a resposta do fechamento de lote

- 71294
  - Ajustes no arquivo strings.xml.
  - Adicionando titulo ao spinner de tipo de valor adicional.
  - Spinner para seleção do tipo da conta.
  - Criação de spinner para selecionar o product short name.
  - Mudando tipo de teclado do planId para text.
  - Trocando posição do spinner do tipo de valor adicional e o input de valor adicional.
  - Deixando, por default, o input do valor adicional desabilitado, caso o spinner seja um valor diferente de 0, habilitar.


- 71280
  - Ajustando dependencias para usar o androidX

  - Adicionando novos campos no pagamento
  - chamada do fechamento de lote com novos campos implementada

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