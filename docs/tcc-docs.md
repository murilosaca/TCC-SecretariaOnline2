UNIVERSIDADE FEDERAL DO PARANÁ

MICHEL ABRIL MARINHO ERICK BERTOLOTTI STELLA HENRIQUE VALDOSKI ALBA NATALI MURILO SANTANA CARDOSO LEONARDO DAVI PAMPLONA

ESPECIFICAÇÃO TÉCNICA DO SISTEMA DA SECRETARIA ONLINE

CURITIBA - PR

2026 MICHEL ABRIL MARINHO ERICK BERTOLOTTI STELLA HENRIQUE VALDOSKI ALBA NATALI MURILO LEONARDO DAVI PAMPLONA

ESPECIFICAÇÃO TÉCNICA DO SISTEMA DA SECRETARIA ONLINE

Especificação de sistema apresentada ao curso de Análise  e  Desenvolvimento  de  Sistemas,  Setor de Educação Profissional e Tecnológica, Universidade Federal do Paraná, como requisito parcial à obtenção  do  título  de  Tecnólogo  em  Análise  e Desenvolvimento de Sistemas.

Orientador: Prof. Dr. Alexander Robert Kutzke

CURITIBA - PR

2026

## RESUMO

A  crescente  demanda  por  eficiência  na  gestão  universitária  evidencia  as  limitações  dos sistemas legados e dos processos manuais ainda em operação em instituições de ensino superior. Considerando  o  contexto  no  Setor  de  Educação  Profissional  e  Tecnológica  (SEPT)  da  UFPR,  o elevado volume de requisições, que vai de ajustes de matrícula a defesas de curso, sobrecarrega as secretarias  e  expõe  fragilidades  na  segurança,  na  rastreabilidade  documental  e  no  registro  de eventos.  Os  métodos  atualmente  empregados  para validação de participação em atividades são suscetíveis a manipulações, comprometendo a integridade das métricas institucionais e penalizando  estudantes e coordenações. Nesse contexto, este trabalho propõe a concepção e o planejamento arquitetural do sistema Secretaria Online 2 (SO2), uma  plataforma digital desenvolvida para modernizar e automatizar os fluxos administrativos do SEPT/UFPR. Durante a fase de análise e modelagem,  foram produzidos artefatos fundamentais que orientam a implementação do sistema, incluindo o mapeamento de domínios de negócio, especificações de requisitos,  diagramas  de  casos  de  uso,  diagramas  de  classes  e  a  definição  de  protocolos  de segurança. Entre os diferenciais do sistema, destaca-se um mecanismo inovador e determinístico para  o  registro  de  presença  em  eventos,  concebido  para  eliminar  fraudes  sem  depender  de sensores  físicos  instáveis,  além  de  um  motor  de  fluxos  transacionais  para  automatizar  trâmites acadêmicos  recorrentes  e  um  módulo  autônomo  de  emissão  de  certificados  com  garantia  de integridade. O planejamento realizado nesta primeira etapa estabelece bases sólidas e consistentes para o desenvolvimento da solução, com potencial de impacto direto na eficiência operacional, na transparência dos processos e na segurança das informações da comunidade acadêmica. Palavras-chave: Gestão Acadêmica; Sistemas de Informação; Engenharia de Software; Automação

Administrativa; Segurança da Informação.

## ABSTRACT

The growing demand for efficiency in university management highlights the limitations of legacy systems and manual processes still operating in higher education institutions. Within the context of the Professional and Technological Education Sector (SEPT) at UFPR, the high volume of requests-ranging from enrollment adjustments to thesis defenses-overloads the administrative offices  and  exposes  vulnerabilities  in  security, document traceability, and event registration. The methods currently employed to validate participation in activities are susceptible to manipulation, compromising the integrity of institutional metrics and penalizing both students and coordinators.In  this  context,  this  work  proposes  the  design  and  architectural  planning  of  the Secretaria  Online  2  (SO2)  system,  a  digital  platform  developed  to  modernize  and  automate  the administrative  workflows  at  SEPT/UFPR.  During  the analysis and modeling phase, fundamental artifacts were produced to guide the system's implementation, including the mapping of business domains,  requirement  specifications,  use  case  diagrams,  class  diagrams,  and  the  definition  of security  protocols.  Among  the  system's  defining  features  is  an  innovative  and  deterministic mechanism  for  event  attendance  registration,  designed  to  eliminate  fraud  without  relying  on unstable  physical  sensors,  as  well  as  a  transactional  workflow  engine  to  automate  recurring academic  procedures  and  an  autonomous  module  for  issuing  certificates  with  guaranteed integrity. The planning carried out in this initial stage establishes solid and consistent foundations for  the  development  of  the  solution,  with  a  potential  direct  impact  on  operational  efficiency, process transparency, and the information security of the academic community.

Keywords :  Academic  Management;  Information  Systems;  Software  Engineering;  Administrative Automation; Information Security.

## SUMÁRIO

| SUMÁRIO.............................................................................................................................................................................. 2  1 INTRODUÇÃO.................................................................................................................................................................. 6  2.  PLANEJAMENTO TRADICIONAL........................................................................................................................... 7  2.1 TERMO DE ABERTURA......................................................................................................................... 7  Introdução..................................................................................................................................................... 7  Gerente de projeto, responsabilidades e autoridade.........................................................................8  Necessidades básicas do trabalho a ser realizado.............................................................................. 8  Descrição do Produto do Projeto............................................................................................................9  Cronograma Básico do Projeto................................................................................................................ 9  Estimativas iniciais de custo................................................................................................................... 10  Necessidades de suporte pela organização........................................................................................ 10  Controle e gerenciamento das informações do projeto..................................................................10  Premissas...................................................................................................................................................... 11  Aprovações................................................................................................................................................... 11  2.2 DIAGRAMAS DE CASO DE USO...............................................................................................................12  2.3 ESTIMATIVAS DE PONTOS POR CASO DE USO.................................................................................12  2.4 WORK BREAKDOWN STRUCTURE.........................................................................................................17  2.5 GRÁFICO DE GANTT................................................................................................................................. 19  3. PLANEJAMENTO ÁGIL............................................................................................................................................. 20  3.1 ESTIMATIVAS DAS HISTÓRIAS DE USUÁRIOS................................................................................... 20  3.2 PLANEJAMENTO DE RELEASES............................................................................................................. 22  4. CONSIDERAÇÕES FINAIS........................................................................................................................................23   |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

REFERÊNCIAS...................................................................................................................................................................24

- 1   CONTEXTUALIZAÇÃO

A crescente demanda por eficiência na administração universitária evidencia as limitações dos sistemas legados e dos processos manuais ainda presentes em instituições de ensino superior. O  volume  elevado  de  requisições  operacionais,  que  abrange  desde  ajustes  de  matrícula  e supervisão de estágios até a validação de atividades complementares e defesas de conclusão de curso,  exige  precisão  cronológica,  rastreabilidade  documental  e  conformidade  normativa.  Nesse cenário,  ferramentas obsoletas que acumulam débitos técnicos e apresentam vulnerabilidades de segurança tornam-se insustentáveis para as exigências contemporâneas de governança institucional (MARTIN, 2017). Assim, este documento tem como objetivo apresentar a especificação técnica para o desenvolvimento de um  sistema que modernize e automatize os fluxos administrativos do Setor de Educação Profissional e Tecnológica (SEPT) da Universidade Federal do Paraná (UFPR). O  sistema  descrito,  denominado  Secretaria  Online 2 (SO2), é uma plataforma digital concebida para substituir  processos analógicos e sistemas legados por um ecossistema seguro, rastreável e escalável.  A  solução  integra  um  portal  web  e  um  aplicativo  móvel  como  interfaces  de  acesso, ambos comunicando-se exclusivamente com um backend centralizado, responsável por orquestrar as  regras  de  negócio  em  domínios  isolados.  Entre  os  principais  componentes  do  sistema, destacam-se: um  motor  de  fluxos transacionais para automação  de  trâmites  acadêmicos recorrentes, um módulo autônomo de emissão de certificados com integridade criptograficamente garantida e um protocolo inovador de registro de presença em eventos, denominado Proof of Stay, concebido para eliminar fraudes sem recorrer a sensores físicos instáveis. O SO2 não substitui o juízo  de  valor  dos  responsáveis  pelos  processos acadêmicos, mas fornece um rastro de auditoria seguro e dados com integridade assegurada para embasar as decisões institucionais. A  especificação  do  sistema  concentra-se  na  análise,  no  planejamento  arquitetural  e  na modelagem  orientada  a  domínio.  Na  etapa  de  planejamento,  foram  definidos  os  requisitos funcionais  e  não  funcionais  e  realizou-se  o  estudo  dos  fluxos  legados  para  mapear  os  gargalos operacionais existentes. Na etapa de modelagem, foram produzidos artefatos baseados na Unified Modeling Language (UML), incluindo o mapeamento de Bounded Contexts, diagramas de casos de uso, especificações de casos de uso, diagramas de classes com foco em objetos de domínio puro e diagramas de estado para os fluxos transacionais. A documentação técnica completa do sistema, incluindo todos os artefatos produzidos, encontra-se organizada nos tópicos deste trabalho.

Os requisitos funcionais, descritos no tópico 4, determinam as funcionalidades essenciais que o sistema deve executar para cumprir seu objetivo principal, abrangendo os fluxos de abertura e tramitação de requerimentos, o gerenciamento  de  eventos  e  o  controle  de  atividades complementares.  Os  requisitos  não  funcionais,  descritos  no  tópico  5,  estabelecem  critérios  de qualidade e restrições operacionais, contemplando  aspectos  de  segurança,  desempenho  e manutenibilidade.

O  diagrama  de  casos  de  uso,  presente  no  tópico  2.2,  descreve  as  funcionalidades  do sistema do ponto de vista dos atores, ilustrando as interações entre Aluno, Docente, Coordenador e  Secretaria  com  os  módulos  da  plataforma.  Os  casos  de  uso  foram  organizados  em  pacotes conforme os domínios funcionais do sistema. As especificações detalhadas de cada caso de uso, com fluxos principal, alternativo e de exceção, encontram-se no mesmo tópico. Os diagramas de classes, presentes no tópico 7, fornecem uma visão estrutural do sistema, elaborados com foco nas entidades de domínio, seus relacionamentos e invariantes. O diagrama concentra-se nos objetos de domínio puro, isolados das camadas de persistência, em consonância com os princípios da Clean Architecture adotada no projeto. A arquitetura do SO2 consolida-se em um modelo de Monólito Modular fundamentado nos preceitos da Clean Architecture, com backend desenvolvido em Kotlin sob o ecossistema Spring Boot. O sistema utiliza PostgreSQL como banco de dados transacional primário, conteinerizado via Docker. Para a modelagem do banco de dados, presente no tópico 9, foram produzidos os modelos conceitual, lógico e físico, assegurando coerência entre os domínios de dados e as funcionalidades previstas,  com  especial  atenção  à  adoção  de  identificadores  UUID  v7  para  garantir  ordenação cronológica e mitigar riscos de enumeração.

- 2   REFERÊNCIAS
- 3   GLOSSÁRIO

BFF  (Backend  for  Frontend) -  Padrão  arquitetural  que  consiste  em  um  serviço  de  backend dedicado a uma interface específica, agregando chamadas e formatando dados para o front-end.

CAAF  (Comissão  de  Atividades  Acadêmicas  Formativas) -  Órgão  acadêmico  responsável  pela avaliação  e  validação  das  atividades  formativas  dos  alunos,  integrado  ao  motor  de  fluxos  do sistema. Clean Architecture -  Filosofia  de  design de software focada na separação de responsabilidades, isolando regras de negócio de frameworks e bancos de dados. COE (Comissão de Estágios) - Órgão responsável pela gestão, acompanhamento e pareceres sobre os estágios curriculares dos cursos. CORS (Cross-Origin Resource Sharing) - Mecanismo de segurança que permite que um servidor autorize origens específicas a acessar seus recursos. ED25519 - Algoritmo de assinatura digital de alta performance e segurança, utilizado para garantir a autenticidade de certificados emitidos. FGAC  (Fine-Grained  Access  Control) -  Modelo  de  controle  de  acesso  granular  que  utiliza capacidades (authorities) específicas em vez de papéis genéricos. GRR -  Identificador  numérico  do  estudante  utilizado  como  registro  acadêmico  institucional  na UFPR. HATEOAS -  Princípio  de  design  de  APIs  REST  onde  o  servidor  fornece  links  na  resposta, orientando o cliente sobre as ações disponíveis. JTI  (JWT ID) -  Identificador  único  associado a um token, utilizado para controle de uso único e blacklist. JWKS  (JSON Web Key Set) -  Estrutura  de  dados  que  contém  um  conjunto  de  chaves  públicas, utilizadas para a verificação de assinaturas digitais. JWT (JSON Web Token) - Padrão para transmitir informações de forma segura e compacta, usado para autenticação no sistema. LGPD (Lei Geral de Proteção de Dados) - Legislação brasileira que regula o tratamento de dados pessoais, aplicada nos requisitos de privacidade do sistema. Monólito Modular - Estilo arquitetural com uma única unidade de implantação, mas estruturado internamente em módulos fracamente acoplados. Outbox Pattern - Padrão para garantir atomicidade entre alterações no banco de dados e disparo de eventos, usando uma tabela temporária. RFC 7807 - Padrão para relatar erros em APIs HTTP de forma consistente e legível. SEPT - Setor de Educação Profissional e Tecnológica da Universidade Federal do Paraná. SHA-256 -  Função  de  hash  criptográfica  utilizada  para  garantir  a  integridade  de  arquivos  e documentos.

SLA  (Service  Level  Agreement) -  Acordo  de  nível  de  serviço  que  define  metas  de  tempo  de resposta para processos administrativos. SO2  (Secretaria Online 2) -  Plataforma digital desenvolvida para automatizar  os  fluxos administrativos do SEPT/UFPR. UML  (Unified  Modeling  Language) -  Linguagem  padrão  de  modelagem  visual  utilizada  para documentar a estrutura e o comportamento do sistema. UUID  v7 -  Identificador  único  universal  com  componentes  temporais,  garantindo  ordenação cronológica. WCAG (Web Content Accessibility Guidelines) - Diretrizes internacionais de acessibilidade para garantir que o conteúdo web seja utilizável por todos.

## 4   REQUISITOS FUNCIONAIS

- 4.1 ACESSO PÚBLICO E AUTENTICAÇÃO

RF-F0-001-a - Autenticação por Credenciais: O sistema deve permitir a autenticação do usuário mediante  a  validação  de  uma  senha  e  de  um  identificador  válido  (e-mail  institucional  @ufpr.br, e-mail pessoal cadastrado ou GRR).

RF-F0-001-b - Emissão de Token JWT: O sistema deve gerar e retornar um token de acesso após a autenticação bem-sucedida das credenciais. RF-F0-001-c  -  Redirecionamento  Baseado  no  Estado da Conta: O  sistema  deve  redirecionar  o usuário autenticado para a interface correspondente ao estado atual de sua conta.

## RF-F0-002 - Solicitar link de recuperação de senha

O sistema deve permitir que um usuário que esqueceu sua senha informe seu e-mail e receba - quando o e-mail estiver cadastrado - um link de redefinição válido por 24 horas, sem revelar se o endereço existe na base de dados.

## RF-F0-003 - Redefinir senha via token de uso único

O sistema deve permitir que um usuário que acessou o link de recuperação de senha defina uma nova  credencial  que  atenda  aos  requisitos  de  segurança,  invalidando  o  token  após  o  uso  e encerrando todas as sessões anteriores.

## RF-F0-004 - Exibir informações de contato da secretaria

O  sistema  deve  exibir  uma  página  pública  com  endereço,  telefones,  e-mail  institucional  e horário de atendimento da secretaria, acessível sem autenticação.

## RF-F0-005 - Exibir página de erro amigável por código HTTP

O sistema deve apresentar telas de erro amigáveis e contextualizadas para os códigos HTTP 401, 403,  404  e  500,  com  mensagens  em  linguagem natural, ações de recuperação e ID de incidente quando aplicável, sem expor detalhes técnicos internos.

## RF-F0-006 - Verificar autenticidade de protocolo PDF

O sistema deve permitir que terceiros verifiquem publicamente a autenticidade de um protocolo de solicitação emitido pelo sistema, consultando metadados sanitizados e comparando localmente o hash SHA-256 de um PDF recebido, sem enviar o arquivo ao servidor.

## RF-F0-007 - Verificar autenticidade de certificado digital

O sistema deve permitir que terceiros verifiquem publicamente a autenticidade de certificados emitidos exclusivamente pelo sistema, validando hash SHA-256 e assinatura digital ED25519 com a chave pública publicada em JWKS, sem aceitar upload de certificados externos como documentos oficiais.

## 4.2 PORTAL DO ALUNO

## RF-F1-001 - Visualizar dashboard unificado do aluno

O sistema deve apresentar ao aluno autenticado um painel unificado com KPIs, pendências de ação, últimas solicitações, próximos eventos, prazos e atalhos - agregados pelo BFF em uma única chamada, com renderização condicional orientada a HATEOAS.

## RF-F1-002 - Completar primeiro acesso (senha + LGPD)

O sistema deve obrigar o aluno com `senha\_alterada = false` a definir senha pessoal forte e aceitar a política de privacidade (LGPD) antes de acessar qualquer outra funcionalidade.

## RF-F1-003-a - Editar dados pessoais do perfil

O sistema deve permitir que o aluno visualize e edite dados pessoais não acadêmicos (nome social, telefone, e-mail pessoal, foto), mantendo campos institucionais somente leitura.

## RF-F1-003-b - Trocar senha e gerenciar sessões ativas

O sistema deve permitir que o aluno troque sua senha (exigindo senha atual) e visualize/encerre sessões ativas em outros dispositivos.

## RF-F1-003-c - Configurar preferências de notificação

O sistema deve permitir que o aluno configure canais de notificação por prioridade, horário DND e modo digest.

## RF-F1-004 - Visualizar e gerenciar comunicações recebidas

O sistema deve exibir hub unificado de comunicações (institucional, turma, inbox de ações) com filtros, marcação de leitura e CTAs para itens que requerem ação.

## RF-F1-005-a - Listar solicitações acadêmicas próprias

O sistema deve permitir que o aluno liste suas solicitações acadêmicas com filtros, indicador de SLA e acesso à criação de nova solicitação quando autorizado.

## RF-F1-005-b - Abrir solicitação via wizard dinâmico

O sistema deve permitir que o aluno abra qualquer tipo de solicitação elegível através de wizard de  3  passos  com  formulário  dinâmico  (`form\_schema`),  anexos  e  confirmação  -  sem  lógica duplicada por tipo.

## RF-F1-005-c - Acompanhar detalhe e timeline de solicitação

O sistema deve exibir detalhe completo da solicitação, timeline de eventos imutável e ações disponíveis exclusivamente via HATEOAS, incluindo edição em ajuste e geração de protocolo PDF.

## RF-F1-006 - Submeter e acompanhar atividades formativas

O  sistema  deve  permitir  que  o  aluno  submeta  comprovantes  de  atividades  formativas, acompanhe  parecer  da  CAAF  e  baixe  certificados  quando  aprovado  -  incluindo  confirmação simplificada para eventos com presença validada.

## RF-F1-007 - Acompanhar estágios e enviar documentos

O  sistema  deve  permitir  que  o  aluno  visualize  estágios  registrados  pela  secretaria,  envie documentos exigidos (TCE, relatórios) e acompanhe pareceres do orientador/COE.

## RF-F1-008 - Acompanhar TCC e enviar versão final

O sistema deve permitir que o aluno acompanhe o status do TCC, visualize banca e datas-chave, e faça upload da versão final quando autorizado.

## RF-F1-009 - Consultar eventos e confirmar presença

O sistema deve permitir que o aluno consulte eventos formativos elegíveis e confirme presença dentro  das  janelas  configuradas,  suportando  modos  SECRET/QR  ×  SINGLE/DUAL  conforme presença v4.1 - sem geofence, trust score ou aula regular.

## RF-F1-010 - Visualizar e baixar certificados emitidos

O  sistema  deve  listar  certificados  emitidos  automaticamente  pelo  sistema  (nunca  upload externo) e permitir download do PDF assinado com QR de verificação pública.

## RF-F1-011 - Consultar atendimentos e dar ciência

O  sistema  deve  permitir  que  o  aluno  visualize  atendimentos  registrados  pela  secretaria  e confirme ciência dos pendentes, com registro auditável.

## 4.3 PORTAL DO EGRESSO

RF-F2-001 - Visualizar dashboard read-only e reemitir documentos O sistema deve oferecer ao egresso autenticado um painel estritamente read-only com histórico acadêmico  resumido,  diploma,  certificados  emitidos  durante  o  curso  e  dados  de  colação, permitindo download do diploma e reemissão de certificados (regeneração do PDF original, sem nova chave de assinatura), com bloqueio de rotas exclusivas de aluno e redirecionamento pós-login adequado ao perfil EGRESSO.

## 4.4 PORTAL DO PROFESSOR

## RF-F3-001 - Visualizar dashboard unificado do professor

O sistema deve apresentar ao professor autenticado um painel unificado com filas de trabalho (solicitações para deliberar, eventos do dia, formativas CAAF, estágios, TCCs), KPIs de SLA e atalhos - agregados pelo BFF em uma única chamada, com blocos e CTAs renderizados exclusivamente a partir de `\_links` HATEOAS.

## RF-F3-002-a - Gerenciar eventos formativos (CRUD)

O sistema deve permitir que o professor com `event.manage` crie, edite, liste e exclua eventos formativos,  configurando  modo  de  presença  (`attendanceMode`),  janelas  de  validação,  carga horária creditada e público-alvo, respeitando regras de imutabilidade por estado do evento.

## RF-F3-002-b - Operar validação de presença ao vivo

O sistema deve permitir que o professor organizador conduza a operação ao vivo do evento - abrir  janelas  de  validação,  exibir  QR  ou  PIN  aos  alunos, acompanhar contagens em tempo real e encerrar o evento disparando emissão automática de certificados.

## RF-F3-003-a - Listar fila de solicitações para deliberação

O sistema deve exibir ao professor deliberante a fila de solicitações que aguardam sua decisão, com indicadores de SLA, filtros e suporte a deliberação em lote quando configurado no workflow.

## RF-F3-003-b - Deliberar solicitação (incl. deep-link JWT)

O  sistema  deve  permitir  que  o  professor  deliberante  defira,  indefira,  solicite  ajustes  ou encaminhe uma solicitação com parecer fundamentado, acessível pela fila ou por deep-link JWT de uso único enviado por e-mail, com registro imutável em auditoria.

## RF-F3-004 - Revisar atividades formativas (CAAF)

O  sistema  deve  permitir  que  professores  membros  da  CAAF  revisem  atividades formativas submetidas por alunos do seu curso, aprovando com horas validadas ou rejeitando com parecer, disparando emissão automática de certificado quando aprovada.

## RF-F3-005 - Emitir pareceres de estágio (orientador/COE)

O  sistema  deve  permitir  que  orientadores  e  membros do COE visualizem estágios sob sua responsabilidade, emitam pareceres por documento e arquivem estágios concluídos.

## RF-F3-006 - Avaliar TCC (orientador/banca)

O  sistema  deve  permitir  que  orientadores  e  membros  de  banca  visualizem  TCCs  sob  sua responsabilidade,  baixem  o  arquivo  final,  registrem  nota  e  parecer,  e  disparem  emissão  de certificado de conclusão quando aprovado e elegível.

## RF-F3-007 - Publicar comunicado para turma ou curso

O sistema deve permitir que o professor redija e publique comunicados em Markdown para turmas  ou  cursos  sob  sua  responsabilidade,  definindo  prioridade  e  expiração,  com  entrega assíncrona via Outbox aos destinatários.

## 4.5 COMISSÕES ACADÊMICAS

## RF-F4-001 - Gerenciar pool CAAF (atribuir e aprovar em lote)

O  sistema  deve  oferecer  aos  membros  da  CAAF  um  painel  do  pool  coletivo  de  atividades formativas submetidas pelos alunos do(s) curso(s) da comissão, permitindo self-assign, atribuição a colegas com visualização de carga, e aprovação em lote para atividades com presença já validada pelo sistema.

## RF-F4-002 - Gerenciar pool COE (atribuir estágios)

O  sistema  deve  oferecer  aos  membros  do  COE  um  painel  do  pool  coletivo  de  estágios aguardando atribuição de orientador, permitindo self-assign e alocação a colegas com visualização de carga, sem aprovação em lote de pareceres (sempre individuais em RF-F3-005).

## 4.6 SECRETARIA ACADÊMICA

## RF-F5-001 - Visualizar dashboard operacional da secretaria

O  sistema  deve apresentar à secretária um painel com KPIs operacionais, fila priorizada de solicitações,  alertas  SLA  e  agenda  de  eventos do dia, agregados pelo BFF e filtrados pelos cursos vinculados ao usuário.

## RF-F5-002-a - Triar fila de solicitações e monitorar atrasados

O sistema deve permitir que a secretária consulte, filtre e priorize a fila central de solicitações dos  cursos  de  sua  competência,  aplique  ações  em  massa  de  atribuição/encaminhamento  e monitore solicitações com SLA vencido com exportação CSV.

## RF-F5-002-b - Abrir solicitação interna em nome do aluno

O sistema deve permitir que a secretária abra solicitações via wizard dinâmico em nome de alunos  dos  cursos  de  sua  competência,  reutilizando  o  fluxo  do  aluno  com  campo  adicional  de titular.

## RF-F5-002-c - Deliberar solicitação (secretaria, sem deep-link)

O sistema deve permitir que a secretária delimere solicitações pela fila (sem deep-link JWT), reutilizando a tela de deliberação do professor com parecer fundamentado e registro em auditoria.

## RF-F5-003 - Gerenciar cadastro de alunos

O sistema deve permitir busca, cadastro, edição, reset de senha e matrícula em disciplinas de alunos, com escopo por curso e auditoria de mutações.

## RF-F5-004-a - Manter cadastro de cursos

O sistema deve permitir CRUD de cursos com vínculo de coordenador, secretários e parâmetros de horas formativas, controlando escopo de `request.view\_curso`.

## RF-F5-004-b - Manter cadastro de disciplinas

O sistema deve permitir CRUD de disciplinas vinculadas a cursos, com código único por curso, carga horária e flag ativa/inativa.

## RF-F5-004-c - Manter períodos e calendário acadêmico

O  sistema  deve  permitir  gestão  de  períodos  letivos  e  eventos  de  calendário  com  tipos semânticos, impedindo sobreposição de períodos e alertando ausência de período vigente.

## RF-F5-005-a - Listar e exportar egressos

O sistema deve listar egressos com filtros e exportação CSV, exibindo situação do diploma e permitindo criação manual excepcional.

## RF-F5-005-b - Registrar colação de grau e entrega de diploma

O sistema deve permitir wizard de colação em lote com validação de elegibilidade, transição ALUNO → EGRESSO, registro de entrega física do diploma e notificação ao egresso.

## RF-F5-006 - Revisar autorizações de imagem em lote

O sistema deve exibir fila compacta de solicitações AUTORIZACAO\_IMAGEM com thumbnails e permitir aprovação/rejeição em lote transacional.

## RF-F5-007 - Registrar atendimento presencial

O sistema deve permitir registro imutável de atendimentos presenciais com busca de aluno, categoria, resposta, anexo opcional e preview de notificação ao aluno.

## RF-F5-008-a - Gerenciar eventos institucionais (CRUD)

O  sistema  deve  permitir  CRUD  de  eventos  formativos  institucionais  no  escopo  dos  cursos vinculados à secretária, reutilizando componentes de RF-F3-002-a.

## RF-F5-008-b - Operar validação de presença (secretaria)

O sistema deve permitir operação ao vivo de eventos pela secretária, paridade funcional com RF-F3-002-b, incluindo encerramento com emissão automática de certificados anti-fraude.

## RF-F5-009 - Importar dados em lote via planilha

O sistema deve oferecer wizard de importação CSV/XLSX com preview linha a linha, validação assíncrona e confirmação transacional por lotes.

## RF-F5-010 - Solicitar exportações assíncronas

O sistema deve permitir solicitar exportações volumosas de forma assíncrona com histórico de jobs, download via URL pré-assinada e notificação por e-mail.

## RF-F5-011 - Visualizar estatísticas operacionais

O sistema deve exibir dashboards quantitativos com gráficos Recharts filtrados por período e curso, drill-down tabular e resumos acessíveis.

## RF-F5-012 - Gerenciar tarefas internas (kanban)

O  sistema  deve  oferecer  kanban  de  tarefas  internas  da  secretaria  (pendente/concluída), controlado por feature flag `tasks.enabled`, sem bloquear demais fluxos do MVP.

## 4.7 COORDENACAO DE CURSO

## RF-F6-001 - Configurar parâmetros curriculares do curso

O sistema deve permitir que o coordenador designado configure parâmetros curriculares do curso - horas formativas mínimas, duração do calendário letivo, regras de banca de TCC e texto de regimento - com auditoria de alterações e sem retroação de elegibilidade já conquistada.

## RF-F6-002 - Visualizar relatórios analíticos de coordenação

O sistema deve oferecer ao coordenador dashboards analíticos com KPIs, gráficos de séries históricas  (evasão,  formativas,  comparativo  entre  cursos,  aprovação  de  formativas),  alertas  de threshold, pendências operacionais e atalhos HATEOAS - estendendo o padrão de RF-F5-011 com métricas exclusivas de coordenação.

## 4.8 ADMINISTRACAO DA PLATAFORMA

## RF-F7-001 - Gerenciar usuários e reset de senha administrativo

O sistema deve permitir que o administrador gerencie todos os usuários (criar, editar, desativar, atribuir perfis via link) e dispare reset de senha por link JWT de uso único - sem nunca visualizar ou definir senhas em texto claro.

## RF-F7-002-a - Gerenciar perfis (roles) e atribuir a usuários

O  sistema  deve  permitir  CRUD  de  perfis  (agregadores  de  authorities),  proteger  perfis pré-definidos  do  sistema  e  atribuir  perfis  a  usuários  via  matriz  checkbox,  invalidando  cache  de capabilities.

## RF-F7-002-b - Gerenciar authorities e matriz FGAC

O sistema deve permitir visualizar e configurar capabilities granulares (authorities) e a matriz role×authority para controle de acesso fino (FGAC) sem alteração de código.

## RF-F7-003 - Configurar tipos de solicitação (workflow engine)

O  sistema  deve  oferecer  editor  de  3  painéis  para  definir  `form\_schema`  (JSON  Schema)  e `workflow\_json` (state machine) de cada tipo de solicitação, com preview ao vivo, versionamento atômico e publicação controlada - núcleo ADR-003 DRY.

## RF-F7-004 - Gerenciar templates de comunicação

O sistema deve permitir CRUD de templates de e-mail/push em Markdown com placeholders dinâmicos, preview com variáveis de exemplo e versionamento imutável por revisão.

## RF-F7-005 - Monitorar Outbox e jobs agendados

O  sistema  deve  exibir  eventos  Outbox  (PENDING/SENT/FAILED/DEAD)  e  jobs agendados, permitindo reentrega manual de eventos falhos e alertando latência do dispatcher.

## RF-F7-006 - Pesquisar trilha de auditoria

O sistema deve permitir pesquisa imutável na trilha de auditoria com filtros por ator, ação, entidade e período, exibindo diff JSON antes/depois em drawer lateral.

## RF-F7-007 - Visualizar saúde do sistema (KPIs operacionais)

O sistema deve exibir KPIs operacionais (latência P95, Outbox pending, erros 5xx, uptime) via Spring Boot Actuator com polling e link para Grafana - fora do MVP (P3).

## 4.9 FUNCIONALIDADES TRANSVERSAIS DE INTERFACE

## RF-F8-001 - Busca global (Command Palette)

O sistema deve permitir que qualquer usuário autenticado abra uma paleta de busca (`Ctrl+K` / ` ⌘ K`  ou  clique  na  topbar),  digite  um  termo  e  encontre  alunos,  solicitações,  eventos ou usuários com  resultados  filtrados  pelas  capabilities  do  token  JWT,  navegando  diretamente  ao  item selecionado.

## RF-F8-002 - Suporte e FAQ com abertura de ticket

O sistema deve permitir que qualquer usuário autenticado consulte FAQ dinâmico em Accordion acessível e, se não  encontrar  resposta,  abra  ticket  de  suporte  com  protocolo  gerado  - preferencialmente via `RequestType=SUPORTE\_TECNICO` (workflow engine DRY).

## 4.10 REQUISITOS TRANSVERSAIS DE ARQUITETURA

## RF-TR-001 - Motor genérico de solicitações (RequestType)

O sistema deve executar o ciclo de vida de todos os tipos de solicitação (meta: 19 tipos) a partir de  configuração  `RequestType`  (`form\_schema` JSON Schema + `workflow\_json` state machine), sem duplicar código por tipo - núcleo ADR-003.

## RF-TR-002 - Hub de comunicação + entrega assíncrona (Outbox)

O sistema deve publicar comunicações institucionais e enfileirar eventos de domínio na tabela `outbox\_event` para entrega assíncrona confiável (in-app, push, e-mail) sem RabbitMQ no MVP.

## RF-TR-003 - Emissão e verificação de certificados anti-fraude

O sistema deve emitir certificados somente a partir de eventos internos validados (presença completa ou formativa aprovada), gerar PDF canônico com hash SHA-256 assinado ED25519 e QR de verificação pública - nunca aceitar upload externo.

## RF-TR-004 - Auditoria imutável de comandos

O sistema deve registrar toda mutação de estado em `audit\_log` append-only, com ator, ação, entidade alvo, payload antes/depois, IP e timestamp - sem endpoints DELETE/PATCH.

## RF-TR-005 - Autorização FGAC + UI orientada a HATEOAS `\_links`

O sistema deve autorizar por capabilities granulares (authorities dot-notation) e expor ações disponíveis via `\_links` HATEOAS - a UI é cega a perfis e renderiza botões somente quando o link existe.

## RF-TR-006 - BFF de dashboard contextual por perfil

O sistema deve agregar em uma chamada HTTP os KPIs, pendências, listas recentes e `\_links` de atalhos do dashboard conforme o perfil ativo, com degradação graciosa se um submódulo falhar.

## RF-TR-007 - Notificações push + e-mail com fallback

O sistema deve entregar notificações por push (FCM/Expo) e e-mail com política de prioridade, agregação (digest), Do Not Disturb e fallback SMTP institucional.

## RF-TR-008 - Presença em eventos formativos v4.1 (modos configuráveis)

O sistema deve suportar confirmação de presença em eventos formativos internos com modos `QR\_SINGLE`, `QR\_DUAL`, `SECRET\_SINGLE`, `SECRET\_DUAL`, janelas temporais configuráveis e device binding - fora do escopo: chamada de aula regular (SIGA).

## 5  REQUISITOS NÃO FUNCIONAIS

## 5.1 SEGURANÇA

## RNF-SEC-01 - Armazenamento de senha com Argon2id

O  sistema  deve  armazenar  todas  as  senhas  de  usuário  exclusivamente  com  o  algoritmo Argon2id, com os parâmetros mínimos recomendados pela OWASP: memória ≥ 47 MB, iterações ≥ 1, paralelismo = 1. É proibido o uso de MD5, SHA-1, SHA-256 ou bcrypt para hashing de senhas.

## RNF-SEC-02 - Autenticação JWT stateless com RS256

O sistema deve emitir tokens de acesso JWT assinados com RS256 (chave privada RSA), com TTL de 15 minutos. O refresh token deve ser opaco (UUID armazenado em banco), com TTL de 7 dias, armazenado  no cliente como cookie `httpOnly; Secure; SameSite=Lax`. O access token deve ser armazenado apenas em memória JavaScript (nunca em `localStorage`).

## RNF-SEC-03 - Refresh token rotativo com detecção de reutilização

A  cada  uso  do  refresh  token,  o  sistema  deve  emitir  um  novo  token  e  invalidar  o  anterior (rotação).  Se  um  token  já  marcado  como utilizado (`isUsed = true`) for apresentado novamente, o sistema deve invalidar todas as sessões do usuário e registrar evento de auditoria `iam.suspicious\_token\_reuse`, indicando possível roubo de token.

## RNF-SEC-04 - Rate limiting em autenticação

O sistema deve limitar tentativas de autenticação para mitigar ataques de força bruta. Na rota `POST /auth/login`, o limite é de 5 tentativas por minuto por par `IP + identificador` (Bucket4j). Após  10  falhas  consecutivas  para  o  mesmo  identificador,  a  conta  é  bloqueada  por  15  minutos (resposta  externa  anti-enumeração idêntica à de credencial inválida). O endpoint de recuperação de senha (`POST /auth/recuperar-senha`) mantém limite de 3 tentativas por hora por IP. Quando o rate limit é excedido, o sistema retorna HTTP 429 com corpo RFC 7807.

## RNF-SEC-05 - Tokens de uso único para deep-links (JWT + JTI blacklist)

Todo  link  enviado  por  email  (recuperação  de  senha,  ação  em  solicitação  por  deep-link  de professor) deve ser protegido por JWT RS256 de uso único, com JTI único armazenado em blacklist após  consumo. O TTL máximo é 24 horas para recuperação de senha e 72 horas para ações de deliberação. O sistema deve rejeitar qualquer tentativa de reutilização com HTTP 401.

## RNF-SEC-06 - Cabeçalhos de segurança HTTP obrigatórios

Todas  as  respostas  HTTP  do backend devem incluir os seguintes cabeçalhos de segurança: `Strict-Transport-Security:  max-age=31536000;  includeSubDomains`;  `X-Content-Type-Options: nosniff`; `X-Frame-Options: DENY`; `Referrer-Policy: strict-origin-when-cross-origin`; `Permissions-Policy: geolocation=(), camera=(), microphone=()`; Content-Security-Policy  com nonce para scripts inline.

## RNF-SEC-07 - FGAC por capabilities - proibido autorização por role

Toda  verificação  de  autorização  deve  ser  feita  por  capability  (authority)  no  formato `dominio.acao` (ex.: `request.deliberate`, `event.manage`), nunca por role (ex.: `ROLE\_SECRETARIO`). O backend deve usar exclusivamente `@PreAuthorize("hasAuthority('dominio.acao')")`. O frontend deve renderizar botões condicionalmente apenas a partir de `\_links` na resposta HATEOAS, sem verificar `user.role`.

## RNF-SEC-08 - Segredos via variáveis de ambiente - zero hardcoded

Nenhuma credencial, chave JWT, senha de banco, API key ou token de serviço externo pode ser hardcoded em código-fonte, `application.yml`, `docker-compose.yml` ou qualquer arquivo versionado. Todos os segredos são injetados via variáveis de ambiente. O arquivo `.env` deve estar no `.gitignore`; o `.env.example` (sem valores reais) deve estar versionado.

## RNF-SEC-09 - Anti-enumeração em endpoints de autenticação

Os  endpoints  de  autenticação  e  recuperação  de  senha  devem retornar respostas idênticas independentemente de o identificador existir ou não no banco, evitando enumeração de usuários. O tempo de resposta para "usuário não encontrado" deve ser equiparado ao de "senha incorreta" com uso de hash dummy (Argon2id dummy verify).

## RNF-SEC-10 - CORS com origens explícitas

A  configuração CORS do backend deve listar explicitamente as origens permitidas a partir de variável  de  ambiente  (`CORS\_ALLOWED\_ORIGINS`).  É  proibido  o  uso  de  `allowedOrigins  = listOf("*")`.  As  origens  permitidas  incluem  o  domínio  do  frontend  web  e  o  scheme  `capacitor:/ /` para o app mobile.

## 5.2 DESEMPENHO

## RNF-DES-01 - Latência P95 de endpoints de listagem

O tempo de resposta P95 de todos os endpoints de listagem paginada (ex.: `GET /solicitacoes`, `GET /eventos`, `GET /usuarios`) deve ser inferior a 300 ms em condições normais de carga (≤ 50 usuários simultâneos).

## RNF-DES-02 - Latência P95 de endpoints de detalhe

O tempo de resposta P95 de todos os endpoints de detalhe (ex.: `GET /solicitacoes/{id}`, `GET /usuarios/{id}`) deve ser inferior a 100 ms em condições normais de carga.

## RNF-DES-03 - Latência total de login

O tempo de resposta P95 do endpoint `POST /auth/login` - incluindo a verificação Argon2id (operação intencional e custosa) - deve ser inferior a 800 ms.

## RNF-DES-04 - Tempo de carregamento do dashboard (FCP e LCP)

O carregamento inicial do dashboard (`/inicio`) deve atingir First Contentful Paint (FCP) inferior a 1,5 s e Largest Contentful Paint (LCP) inferior a 3 s em conexão 4G simulada (10 Mbps, 40ms RTT).

## RNF-DES-05 - Latência de despacho do Outbox

O  tempo entre a inserção de um evento na tabela `outbox\_event` (status `PENDING`) e seu despacho  efetivo  (status  `SENT`)  deve  ser  inferior  a  30  segundos  em  condições  normais.  O dispatcher é executado a cada 5 segundos pelo `@Scheduled`.

## RNF-DES-06 - Toda coleção retornada deve ser paginada

Nenhum endpoint de listagem pode retornar coleções sem paginação. Todo `GET /{resource}` deve  aceitar  parâmetros  `page`,  `size`  (padrão  20,  máximo  100)  e  `sort`.  Queries  sem  `Pageable` devem falhar no ArchUnit.

## 5.3 DISPONIBILIDADE

## RNF-DIS-01 - Backup diário automático do PostgreSQL

O  banco  de  dados  PostgreSQL  deve  ter  backup  diário  automático  no  formato  `pg\_dump --format=custom`, armazenado no MinIO (bucket `backups`), com retenção mínima de 14 dias. O backup deve ser executado via cron ou GitHub Actions scheduled workflow.

## RNF-DIS-02 - Restore testado mensalmente

O procedimento de restore do banco de dados deve ser executado e validado em ambiente de staging pelo menos uma vez por mês, garantindo que os backups são efetivamente utilizáveis em caso de desastre.

## RNF-DIS-03 - Health checks e probes de readiness/liveness

O  backend  deve  expor  os  endpoints  de  saúde via  Spring  Boot  Actuator:  `/actuator/health` (público, retorna `UP`/`DOWN`), `/actuator/health/liveness` e `/actuator/health/readiness` (para eventual  deploy  em  Kubernetes).  Os  detalhes  completos  do  health  check  devem ser acessíveis apenas para usuários autenticados com `system.observe`.

## RNF-DIS-04 - Observabilidade: alertas críticos de operação

O sistema de monitoramento deve ter alertas Prometheus/Grafana configurados para: (a) taxa de erros 5xx &gt; 1% em janela de 5 minutos; (b) latência P99 &gt; 2 s em janela de 5 minutos; (c) fila Outbox pendente &gt; 200 eventos por mais de 2 minutos; (d) pool de conexões HikariCP com &gt; 5 conexões pendentes por mais de 1 minuto.

- 5.4 USABILIDADE E ACESSIBILIDADE

## RNF-UX-01 - WCAG 2.1 Nível AA

Todas  as  telas  do  sistema  devem  conformar  com  WCAG  2.1  Nível  AA.  Requisitos  mínimos: contraste ≥ 4,5:1 para texto normal, ≥ 3:1 para texto grande (≥ 18pt); todos os elementos interativos acessíveis via teclado; `aria-live="polite"` em conteúdo dinâmico (erros de formulário, atualizações de status); `role="dialog"` + `aria-labelledby` + focus trap em modais.

## RNF-UX-02 - Responsividade a partir de 375px

Todas  as  telas  do  sistema  web  devem  ser  funcionais  e  legíveis  em  dispositivos  com  largura mínima de 375px (iPhone SE). Nenhum conteúdo pode ser truncado ou sobrepostos em 375px. A navegação lateral deve colapsar para overlay em telas menores que `lg` (1024px).

## RNF-UX-03 - Navegação completa por teclado

Todos  os  fluxos  críticos  (login,  nova  solicitação,  deliberação)  devem  ser  completáveis exclusivamente com teclado. A ordem de tabulação deve ser lógica e seguir a ordem visual dos elementos.  O  anel  de  foco  (`focus  ring`)  deve  ser  sempre  visível  (`ring-2  ring-brand-primary`) quando o elemento tem foco via teclado.

## RNF-UX-04 - UI orientada a capacidades via HATEOAS

O frontend nunca deve renderizar botões de ação ou elementos de navegação com base no perfil (`user.role`)  do usuário. Toda renderização condicional de ações deve ser baseada na presença do link  correspondente  em  `\_links`  da  resposta  da  API  (padrão  HATEOAS),  consumido  via  o  hook `useActions(resource)`.

## RNF-UX-05 - Estados de componente obrigatórios (loading, empty, error)

Todo componente que exibe dados carregados de API deve implementar obrigatoriamente três estados: (a) loading - exibir `DS/Skeleton` com a mesma estrutura visual do estado preenchido; (b) empty - exibir `DS/EmptyState` com mensagem descritiva e, quando pertinente, ação de criação; (c) error - exibir `DS/AlertBanner variante "danger"` com opção de retry.

## 5.5 MANUTENIBILIDADE

## RNF-MAN-01 - Cobertura de testes mínima

O projeto deve manter cobertura mínima de testes automatizados: camada de domínio ≥ 85% (JUnit 5 + Kotest, puro Kotlin sem Spring), camada de aplicação (casos de uso) ≥ 70% (MockK), total do backend ≥ 75%. O frontend deve ter cobertura de hooks e utilitários ≥ 70% (Vitest).

## RNF-MAN-02 - Zero violações de lint e format

O código-fonte deve passar sem erros nas ferramentas de lint e formatação: backend com `ktlint` (formatação) e `detekt` (análise estática); frontend com `eslint` e `prettier`. Violações do tipo `error` bloqueiam o merge. Violações `warning` são toleradas se justificadas em comentário.

## RNF-MAN-03 - Complexidade ciclomática ≤ 10

Nenhuma  função  ou  método  deve  ter  complexidade  ciclomática  de  McCabe  superior a 10. Funções com complexidade &gt; 10 devem ser refatoradas antes do merge.

## RNF-MAN-04 - DRY: blocos duplicados &lt; 3%

A  porcentagem de blocos de código duplicados no projeto deve ser inferior a 3%, medida por ferramenta  de  análise  estática.  A  estratégia  DRY  é  central  para  o  TCC:  o  motor  genérico  de workflow substitui 57 telas quase idênticas do legado; o `DynamicForm` renderiza 19 tipos; value objects evitam validação duplicada de CPF, email e GRR.

## RNF-MAN-05 - Clean Architecture enforced por ArchUnit

As  regras  de  dependência  da  Clean  Architecture  devem  ser verificadas automaticamente por ArchUnit nos testes: (a) o pacote `domain/` não pode importar nenhuma classe de `org.springframework`, `jakarta.persistence`, ou `org.hibernate`; (b) o pacote `infrastructure/` de um módulo não pode ser importado pelo `infrastructure/` de outro módulo; (c) módulos comunicam-se apenas por interfaces em `application/ports/`.

## 5.6 CONFIABILIDADE RNF-CON-01 - Padrão Outbox: atomicidade garantida para notificações

Toda mudança de estado que deve gerar notificação (email, push) deve persistir o evento na tabela `outbox\_event` na mesma transação da mudança de estado da entidade principal. É proibido enviar  email/push  de  forma  síncrona  ou  em  transação  separada.  O  dispatcher  `@Scheduled` processa a fila de forma assíncrona e independente.

## RNF-CON-02 - Migrations Flyway imutáveis e versionadas

Toda alteração de schema  de banco de dados  deve  ser  feita  por  arquivo  Flyway `V###\_\_descricao.sql` com número de versão incrementado. É proibido editar qualquer arquivo de  migration  já  aplicado  a  qualquer  ambiente.  Correções  devem  ser  novas  migrations  (ex.: `V012\_\_fix\_index\_request.sql`). A validação Flyway (`spring.flyway.validate-on-migrate=true`) deve estar sempre ativa.

## RNF-CON-03 - Respostas de erro no formato RFC 7807 Problem Details

Todas as respostas de erro HTTP 4xx e 5xx devem ter Content-Type `application/problem+json` e body conformando com RFC 7807, contendo no mínimo os campos: `type` (URI de documentação do erro), `title` (descrição curta), `status` (código HTTP), `detail` (mensagem humanizada), `instance` (URI da requisição). O campo `errors` pode ser adicionado para erros de validação (HTTP 422).

## RNF-CON-04 - Eliminação de queries N+1

Nenhum endpoint de listagem ou detalhe pode resultar em problema de N+1 queries (uma query adicional  por  item  da  lista).  Todos  os  relacionamentos  necessários  para  resposta  devem  ser carregados  em  um  único  `JOIN  FETCH`  ou  via  `@EntityGraph`.  Queries  de  leitura  devem  usar projeções (`interface Projection`) para listas.

## 5.7 PORTABILIDADE

## RNF-POR-01 - Plataformas-alvo: Web e Mobile nativo

O sistema deve funcionar em duas plataformas: (a) Web: React 18 + Vite, compatível com os navegadores listados em RNF-CMP-03; (b) Mobile: React Native + Expo SDK 50+, compatível com Android 10+ e iOS 15+. Funcionalidades P0 (login, dashboard, nova solicitação, confirmar presença) devem estar disponíveis em ambas as plataformas.

## RNF-POR-02 - Ambiente de desenvolvimento reproducível (Docker Compose)

O ambiente de desenvolvimento completo (PostgreSQL, MinIO, Mailpit, backend, Prometheus, Grafana, Loki) deve ser inicializável  com um único comando `docker compose up -d` a partir do diretório raiz. O README deve documentar os 5 comandos necessários para subir tudo do zero.

## RNF-POR-03 - Armazenamento de arquivos via API S3-compatível

Todo  armazenamento  de  arquivos  (anexos  de  solicitação,  comprovantes  de  formativas, certificados  PDF  gerados)  deve  usar  a  API  S3-compatível  via  MinIO  (desenvolvimento/TCC) ou AWS  S3 (produção), configurável por variável de ambiente `STORAGE\_ENDPOINT`. O código de aplicação não deve referenciar o sistema de arquivos local.

## 5.8 COMPATIBILIDADE

## RNF-CMP-01 - API documentada em OpenAPI 3.x

Todos os endpoints da API REST devem ser documentados no padrão OpenAPI 3.x, gerados automaticamente pelo SpringDoc 2.x a partir das anotações `@Operation`, `@ApiResponse` e `@Tag`. O  Swagger  UI  deve  estar  acessível  em  `/swagger-ui`  em  todos  os  ambientes  não-produção.  Os tipos TypeScript do frontend devem ser gerados a partir da spec OpenAPI via `openapi-typescript`.

## RNF-CMP-02 - PostgreSQL 16 com extensões obrigatórias

O banco de dados deve ser PostgreSQL 16 (mínimo 14) com as extensões: `uuid-ossp` (função `uuid\_generate\_v7()`), `pgcrypto`  (geração  segura  de  dados  aleatórios),  `citext`  (comparação case-insensitive para email), `pg\_trgm` (busca por similaridade textual). A migration `V000\_\_extensions.sql` deve ser a primeira a ser aplicada.

## RNF-CMP-03 - Compatibilidade com navegadores modernos

O frontend web deve ser compatível com as últimas 2 versões dos navegadores: Chrome, Firefox, Safari  e  Edge.  O  transpile  via  Vite  deve  garantir  compatibilidade  sem  polyfills  manuais.  É  aceita degradação  graceful  (não  quebra, mas pode omitir recursos visuais avançados) em IE e versões muito antigas.

## 9 CONFORMIDADE LEGAL

## RNF-LGL-01 - LGPD: tratamento mínimo de dados pessoais

O sistema deve: (a) coletar apenas os dados pessoais necessários para a finalidade específica de cada funcionalidade (princípio da minimização); (b) registrar o momento do aceite de termos/LGPD  do  usuário  no  campo  `usuario.metadata.aceite\_lgpd\_em`  com  timestamp;  (c) permitir  que  o  usuário  exporte  seus  dados  pessoais  via  tela  de  perfil;  (d)  não  logar  CPF,  GRR completo, senha ou dados bancários em logs de aplicação.

## RNF-LGL-02 - Certificados gerados pelo sistema com trilha criptográfica

Todos  os  certificados  de  participação  emitidos  pelo  sistema  devem:  (a)  ser  gerados automaticamente pelo sistema quando o evento conclui (nunca via upload de PDF externo); (b) ter hash SHA-256 do PDF canônico armazenado em `certificate.hash\_sha256`; (c) ser assinados com chave ED25519 do servidor; (d) conter QR Code apontando para `/publico/verificar-certificado/:hash`;  (e)  chave  pública  publicada  em  `/.well-known/jwks.json` para verificação offline.

## 6   DIAGRAMA DE CASOS DE USO

<!-- image -->

## 7   DIAGRAMA DE CLASSES

<!-- image -->

## 8  HISTÓRIAS  DE  USUÁRIO,  TELAS  E  DIAGRAMAS  DE  SEQUÊNCIA POR

## MÓDULO 8.1 PÚBLICO

## HU 1. US-F0-001 - Autenticação de Usuário (Login)

COMO QUERO me autenticar informando meu identificador (e-mail institucional ou GRR) e minha senha PARA

um usuário do sistema (Aluno, Egresso, Professor, Secretária, Coordenador ou Administrador) acessar o painel com as funcionalidades correspondentes ao meu perfil de forma segura. DESENHO DA(S) TELA(S) FIGURA 3 - Tela de login (web).

<!-- image -->

FIGURA 4 - Tela de login (mobile).

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

## Critério 1: Login com sucesso (fluxo principal)

DADO QUE o usuário está na tela de login (/login) possui conta ativa com senha já definida (senha\_alterada = true)

QUANDO preenche o campo "Email ou GRR" com identificador válido preenche o campo "Senha" com a senha correta clica no botão "Entrar" ENTÃO o componente DS/Button exibe estado "loading" (spinner + label "Entrando...") o sistema realiza POST /auth/login com os campos como JSON ao receber 200 OK armazena o access token e o refresh token redireciona para /inicio (F1.1) o evento iam.login\_success é registrado na tabela audit\_log

## Critério 2: Primeiro acesso (redirecionamento obrigatório)

DADO QUE o usuário está em /login a conta tem senha\_alterada = false QUANDO informa credenciais corretas e clica em "Entrar" ENTÃO o sistema recebe mustChangePassword = true na resposta o frontend bloqueia o acesso ao dashboard redireciona para /primeiro-acesso (F1.2) não é possível navegar para /inicio até que a senha seja alterada

## Critério 3: Credenciais inválidas (anti-enumeração)

DADO QUE o usuário está em /login QUANDO informa identificador válido com senha incorreta OU informa identificador inexistente na base de dados clica em "Entrar" ENTÃO

o sistema retorna 401 Unauthorized (RFC 7807) exibe DS/AlertBanner variante "danger" com mensagem genérica: "Credenciais inválidas. Verifique seus dados e tente novamente." o campo "Email ou GRR" permanece preenchido o campo "Senha" é limpo automaticamente a mensagem não revela se o identificador existe ou não o evento iam.login\_failed é registrado na tabela audit\_log

## Critério 4: Rate limit atingido

DADO QUE o mesmo IP + identificador realizou 5 tentativas de login em menos de 1 minuto QUANDO uma nova tentativa é realizada ENTÃO o sistema retorna 429 Too Many Requests exibe DS/AlertBanner variante "warning": "Muitas tentativas. Aguarde antes de tentar novamente." o botão "Entrar" permanece desabilitado enquanto o bloqueio temporário estiver ativo

## Critério 5: Bloqueio de conta por tentativas excessivas

DADO QUE o identificador acumulou 10 falhas consecutivas de autenticação QUANDO uma nova tentativa de login é feita para esse identificador ENTÃO o sistema retorna 401 com mensagem genérica (mesma de CA-03, sem revelar bloqueio) internamente registra o evento iam.account\_blocked a conta permanece bloqueada por 15 minutos

## Critério 6: Validação de campos vazios (frontend)

DADO QUE o usuário está em /login com o formulário vazio QUANDO clica no botão "Entrar" sem preencher os campos ENTÃO o campo vazio exibe borda "border/error" e mensagem de validação inline o sistema NÃO realiza chamada à API o foco é direcionado ao primeiro campo inválido

## Critério 7: Links de navegação da tela de login

DADO QUE o usuário está em /login QUANDO clica no link "Esqueci minha senha" ENTÃO é redirecionado para /recuperar-senha (F0.2) QUANDO clica no link "Contato" ENTÃO é redirecionado para /contato (F0.4) QUANDO clica no link "Verificar protocolo ou certificado" ENTÃO é redirecionado para /publico/verificar-protocolo (F0.6)

## Critério 8: Acessibilidade

DADO QUE o usuário navega pela tela de login usando apenas o teclado ENTÃO a ordem de tabulação segue: identificador → senha → "Esqueci minha senha" → "Entrar" → links o toggle de visibilidade da senha possui aria-label descritivo o DS/AlertBanner de erro possui aria-live="polite" todos os textos visíveis têm contraste mínimo 4.5:1 (WCAG 2.1 AA)

## Critério 9: Responsividade mobile

DADO QUE o usuário acessa /login em dispositivo com largura 375px ENTÃO o card de autenticação ocupa toda a largura com margem horizontal de 16px (space/md) o botão "Entrar" não é encoberto pelo teclado virtual (safe area) todos os elementos são interativos com touch (target mínimo 44px)

## DIAGRAMAS DE SEQUÊNCIA F0.1-a - Login happy path

Escopo: happy path Atores: Usuário (qualquer perfil), WebApp, AuthController, LoginUseCase, Postgres Pré-condições: conta ativa, senha\_alterada = true, menos de 5 tentativas no último minuto Notas: Passo 1:  identificador  aceita  e-mail  @ufpr.br  ou  GRR  numérico  -  normalizado  antes  do SELECT (RN-F0.1-01). Passo 6: Argon2id substitui o MD5 legado; auto-call representa verificação local no UseCase, sem round-trip à DB (RN-F0.1-02). Passo  10:  WebApp  armazena  accessToken  em  memória  e  refreshToken  em  cookie  httpOnly  + SameSite=Lax; mobile: Keychain/Keystore (RN-F0.1-03).

<!-- image -->

## F0.1-b - Login → primeiro acesso (mustChangePassword)

Escopo: happy path - variação com senha\_alterada = false Atores: Usuário, WebApp, AuthController, LoginUseCase, Postgres Pré-condições: conta ativa, senha\_alterada = false (primeiro acesso ou reset administrativo)

<!-- image -->

Notas: Passo 10: WebApp bloqueia toda navegação para rotas protegidas enquanto mustChangePassword=true não for resolvido (RN-F0.1-04). O  token  emitido  é  válido  -  permite  autenticar  o  formulário  em  /primeiro-acesso;  só  /inicio  e demais rotas são bloqueadas. Fluxo de /primeiro-acesso coberto em US-F1-002.

## F0.1-c - Login 401 - credenciais inválidas (anti-enumeração)

Escopo: erro 401 Atores: Usuário, WebApp, AuthController, LoginUseCase, Postgres Pré-condições:  identificador  inexistente  ou  senha  incorreta  (&lt;  10  falhas  consecutivas,  &lt;  5 tentativas/min)

<!-- image -->

Notas: Passo  6:  tanto  "senha  errada"  quanto  "usuário  não  encontrado"  resultam  na  mesma  exceção  e mesma resposta HTTP - nenhum detalhe distinguível ao cliente (RN-F0.1-08). Passo 9: corpo RFC 7807 - type: .../errors/unauthorized, detail: "Credenciais inválidas. Verifique seus dados e tente novamente.". Campo identificador mantido preenchido; campo senha é limpo pelo WebApp.

## F0.1-d - Login 429 - rate limit atingido

Escopo: erro 429 Atores: Usuário, WebApp, RateLimitFilter, AuthController Pré-condições: mesmo IP + identificador realizou ≥ 5 tentativas em &lt; 1 minuto Notas: Passo  3:  RateLimitFilter  é  um  OncePerRequestFilter  executado  antes  do  AuthController;  o LoginUseCase nunca é invocado (RN-F0.1-06). Passo 4: corpo RFC 7807 inclui retryAfterSeconds para o frontend exibir countdown (RN-F0.1-09). Bucket4j  usa  sliding  window  por  combinação  IP  +  identificador;  IPs  diferentes  para  o  mesmo identificador têm contadores independentes.

<!-- image -->

## F0.1-e - Login 401 - conta bloqueada

Escopo: erro 401 - bloqueio temporário por tentativas excessivas Atores: Usuário, WebApp, AuthController, LoginUseCase, Postgres Pré-condições: identificador acumulou 10 falhas consecutivas de autenticação Notas: Passo 9: a resposta HTTP é idêntica à de credenciais inválidas (CA-03) - sem revelar ao cliente que a conta está bloqueada (RN-F0.1-08). Passo 7: iam.account\_blocked é o evento interno; o campo bloqueado\_ate é verificado no início do fluxo nas tentativas seguintes (desbloqueio automático após 15 min). Este fluxo pressupõe que o rate limit (5/min) não bloqueou antes - os 10 erros acumularam em múltiplas janelas de tempo (&gt; 2 min total).

<!-- image -->

## F0.1-f - Refresh token - reuso detectado (revogação de sessões)

Escopo: erro 401 - defesa contra roubo de token Atores: WebApp, AuthController, RefreshTokenUseCase, Postgres Pré-condições: cliente apresenta um refresh token já rotacionado (expirado por rotação)

<!-- image -->

Notas: Passo 3: SELECT FOR UPDATE evita race condition em caso de apresentação simultânea do token reutilizado por dois agentes (RN-F0.1-10). Passo 5: revogação de todas as sessões ativas do usuário - não apenas a sessão corrente - é a defesa contra roubo de cookie (sub-fluxo F0.1). O fluxo normal de rotação de refresh token (token válido → emite novo par) é o caminho inverso a este e não requer diagrama separado - o comportamento está implícito no happy path (passo 7 de F0.1-a).

## HU 2. US-F0-002 - Solicitar Link de Recuperação de Senha

COMO um usuário cadastrado no sistema que não lembra sua senha QUERO informar meu e-mail e receber um link de redefinição PARA

recuperar o acesso à minha conta sem precisar contatar a secretaria. DESENHO DA(S) TELA(S) FIGURA 5 - Tela de recuperar senha (web).

<!-- image -->

FIGURA 6 - Tela de recuperar senha (mobile).

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Fluxo principal (e-mail cadastrado)

DADO QUE o usuário está em /recuperar-senha QUANDO preenche o campo "E-mail" com um e-mail válido e cadastrado no sistema clica em "Enviar link" ENTÃO o sistema realiza POST /auth/forgot-password recebe 202 Accepted o formulário é ocultado exibe DS/AlertBanner variante "info" com ícone Mail e mensagem: "Se este e-mail estiver cadastrado, você receberá um link válido por 24 horas." internamente um JWT de uso único é gerado e enfileirado para envio por e-mail o evento iam.password\_reset\_requested é registrado em audit\_log

## Critério 2: E-mail não cadastrado (resposta idêntica)

DADO QUE o usuário está em /recuperar-senha QUANDO preenche o campo "E-mail" com um e-mail que NÃO existe na base de dados clica em "Enviar link" ENTÃO o sistema realiza POST /auth/forgot-password e recebe 202 Accepted exibe exatamente a mesma mensagem do CA-01 (anti-enumeração) NENHUM e-mail é enviado internamente a resposta visual é IDÊNTICA à de um e-mail cadastrado

## Critério 3: Validação de formato de e-mail (frontend)

DADO QUE o usuário está em /recuperar-senha QUANDO preenche o campo "E-mail" com valor inválido (ex: "abc", "sem@domínio") clica em "Enviar link" ENTÃO

o sistema NÃO realiza chamada à API exibe mensagem de validação inline: "Informe um e-mail válido" o campo exibe borda "border/error"

## Critério 4: Estado de loading durante submit

DADO QUE o usuário preencheu o formulário e clicou em "Enviar link" QUANDO a chamada à API está em andamento ENTÃO o botão "Enviar link" exibe estado "loading" (spinner + label "Enviando...") o campo de e-mail fica desabilitado durante o processamento

## Critério 5: Erro de rede

DADO QUE o usuário está em /recuperar-senha QUANDO tenta enviar o formulário e ocorre falha de conexão ou timeout ENTÃO o sistema exibe DS/AlertBanner variante "danger": "Erro ao processar a solicitação. Verifique sua conexão e tente novamente." o formulário permanece acessível para nova tentativa

## Critério 6: Navegação para o login

DADO QUE o usuário está em /recuperar-senha QUANDO clica no botão/ícone "Voltar" (variante ghost + ícone ArrowLeft) ENTÃO é redirecionado para /login (F0.1)

DIAGRAMAS DE SEQUÊNCIA

## F0.2-a - Solicitar reset (e-mail cadastrado) - happy path

Escopo: happy path - e-mail existe na base de dados Atores: Usuário, WebApp, AuthController, ForgotPasswordUseCase, Postgres Pré-condições:  e-mail  com  formato  válido (validado no frontend), conta ativa, &lt; 3 tentativas na última hora

<!-- image -->

Notas: Passo  6:  o  JWT  é  gerado  em  memória  (não  persiste  na  DB  antes  do  uso);  a  blacklist  de  JTI  é populada apenas no consumo do token - coberto em US-F0-003 (RN-F0.2-02).

Passos  7-10:  TX  atômica  garante  que  o  outbox\_event  só  existe  se  o  audit\_log  foi  escrito  (e

vice-versa); nenhum e-mail é enviado sincronamente (RN-F0.2-03). Dispatch  assíncrono  do  outbox\_event  (template  PASSWORD\_RESET  via  MailAdapter):  DRY → transversal/10.1-outbox-notificacao.md. Passo  13:  a  resposta  visual  é  idêntica  à  do  CA-02  -  nem  o  frontend  nem  o  usuário  conseguem

distinguir se o e-mail existia (RN-F0.2-01).

## F0.2-b - Solicitar reset (e-mail não cadastrado) - anti-enumeração

Escopo: sequência - e-mail informado não existe na base de dados Atores: Usuário, WebApp, AuthController, ForgotPasswordUseCase, Postgres

Pré-condições: e-mail com formato válido, não cadastrado no sistema

<!-- image -->

Notas: Passo  7:  nenhum  JWT  é  gerado  e  nenhum  outbox\_event  é  inserido  -  zero  e-mails  serão disparados (CA-02). O audit\_log registra a tentativa mesmo com e-mail inexistente, com campo email ofuscado em logs (RN-F0.2-07). A resposta HTTP  202  e o texto do banner  são  bit-a-bit  idênticos  ao  F0.2-a  -  defesa

anti-enumeração de contas (RN-F0.2-01).

## F0.2-c - Solicitar reset - 429 rate limit

Escopo: erro 429 - proteção contra spam de e-mails de recuperação Atores: Usuário, WebApp, RateLimitFilter Pré-condições: mesmo e-mail + IP realizou ≥ 3 solicitações na última hora

<!-- image -->

Notas: Passo  3:  RateLimitFilter  executa  antes  do  AuthController;  o  ForgotPasswordUseCase  nunca  é invocado (RN-F0.2-08). Janela  de  1  hora  é  mais  restritiva  que  o  login  (1  min)  -  objetivo  é  mitigar  spam  de  e-mails, não ataques de força bruta de credencial. Resposta  RFC  7807  inclui  retryAfterSeconds  para  o  frontend  calcular  quando  liberar  o  botão novamente.

## HU 3. US-F0-003 - Definir Nova Senha via Token

COMO um usuário que clicou no link de redefinição recebido por e-mail QUERO

definir uma nova senha que atenda aos requisitos de segurança PARA

recuperar o acesso à minha conta com uma credencial que só eu conheço.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Fluxo principal (token válido, senha forte)

DADO QUE o usuário acessou /nova-senha?token=&lt;JWT\_válido\_não\_consumido&gt; o token não está expirado e o JTI não está na blacklist QUANDO o frontend carrega a tela ENTÃO exibe o formulário: campo "Nova senha", medidor de força e campo "Confirmar senha" a lista de requisitos (mínimo 12 chars, maiúscula, minúscula, número, especial) está visível

## Critério 2: Medidor de força de senha em tempo real

DADO QUE o formulário de nova senha está visível QUANDO o usuário digita no campo "Nova senha" ENTÃO o componente DS/Progress (medidor de força) atualiza em tempo real: - 1 segmento (danger): &lt; 8 chars ou critérios insuficientes - 2 segmentos (warning): critérios parciais

- 3 segmentos (warning/success): maioria dos critérios - 4 segmentos (success): todos os critérios atendidos

cada requisito na lista exibe ícone ✓ verde quando cumprido individualmente

## Critério 3: Validação de confirmação de senha (frontend)

DADO QUE o usuário preencheu "Nova senha" e "Confirmar senha" com valores diferentes QUANDO clica em "Salvar senha" ENTÃO o sistema NÃO realiza chamada à API exibe mensagem de erro inline sob "Confirmar senha": "As senhas não coincidem" o campo "Confirmar senha" exibe borda "border/error"

## Critério 4: Senha igual a uma das 3 últimas (rejeição do backend)

DADO QUE o usuário informou uma nova senha igual a uma das últimas 3 senhas cadastradas QUANDO clica em "Salvar senha" e o backend processa a requisição ENTÃO o sistema retorna 422 Unprocessable Entity (RFC 7807) exibe DS/AlertBanner variante "danger": "Esta senha já foi utilizada recentemente. Escolha uma senha diferente." o campo "Nova senha" é limpo

## Critério 5: Conclusão bem-sucedida

DADO QUE o usuário preencheu "Nova senha" com senha que atende todos os requisitos "Confirmar senha" é igual à "Nova senha" QUANDO

clica em "Salvar senha" ENTÃO o sistema realiza POST /auth/reset-password { token, novaSenha } ao receber 200 OK: - o JTI é inserido na blacklist (sem possibilidade de re-uso) - todas as sessões ativas do usuário são invalidadas - usuario.senha\_alterada é definido como true - o evento iam.password\_reset\_completed é gravado em audit\_log o usuário é redirecionado para /login

/login exibe DS/AlertBanner success: "Senha redefinida com sucesso."

## Critério 6: Token inválido ou expirado

DADO QUE o usuário acessou /nova-senha?token=&lt;JWT\_expirado\_ou\_inválido&gt; QUANDO o frontend tenta validar o token com o backend ENTÃO o formulário NÃO é exibido exibe DS/EmptyState com: - ícone de alerta - título: "Link inválido ou expirado" - descrição: "Este link não é mais válido. Solicite um novo para redefinir sua senha." - botão primário: "Solicitar novo link" → /recuperar-senha

## Critério 7: Token já utilizado (JTI na blacklist)

DADO QUE o usuário acessou /nova-senha?token=&lt;JWT\_já\_consumido&gt; QUANDO o frontend tenta validar o token com o backend ENTÃO o comportamento é idêntico ao CA-06 (token inválido)

a mensagem NÃO revela se o token foi "já usado" ou apenas "inválido"

## DIAGRAMAS DE SEQUÊNCIA

## F0.3-a - Redefinição bem-sucedida (happy path)

Escopo: happy path - token válido, senha nova forte, sem reuso

Atores: Usuário, WebApp, AuthController, ResetPasswordUseCase, Postgres

Pré-condições:  token  JWT  não  expirado,  JTI  não  consumido,  nova  senha  ≥  12  chars  com complexidade, diferente das últimas 3

<!-- image -->

## Notas:

Passo  4:  JWT  verification  (assinatura  HMAC/RSA,  audience="password-reset",  exp)  ocorre  em memória no UseCase antes de qualquer acesso à DB; o SELECT combina a leitura do blacklist e do usuário (RN-F0.3-01).

Passo 4 (check histórico): após receber passwordHistory, UC verifica internamente se Argon2id.verify(novaSenha, histHash) retorna falso para cada uma das 3 últimas - computação em memória, sem round-trip adicional (RN-F0.3-06).

Passo 7: Argon2id.hash(novaSenha) é calculado antes do BEGIN TX (CPU-bound); o hash resultante é inserido no UPDATE (RN-F0.3-02).

Passos 7-11: transação atômica - se qualquer INSERT falhar, o hash não é persistido e o JTI não é colocado em blacklist (idempotência garantida pela unicidade do JTI).

Passo 8: invalidação de todas as sessões ativas (refresh tokens) - o usuário precisa fazer novo login em todos os dispositivos (RN-F0.3-08).

Passo 9: JTI inserido em iam\_jti\_blacklist torna re-uso impossível (RN-F0.3-03).

## F0.3-b - 401 token inválido, expirado ou já consumido

Escopo: erro 401 - cobre CA-06 (token inválido/expirado) e CA-07 (JTI já na blacklist)

Atores: Usuário, WebApp, AuthController, ResetPasswordUseCase, Postgres

Pré-condições:  token  JWT  tem  assinatura  inválida,  expirou,  ou  JTI  já  foi  consumido  em  reset anterior

<!-- image -->

## Notas:

Passo 4: a DB--&gt;&gt;UC retorna JTI presente para CA-07 (token reutilizado). Para CA-06 (assinatura inválida ou exp expirado), a JWT verify falha em memória antes do SELECT - o fluxo para no passo 3  sem  chegar  ao  DB;  o  resultado  é  o  mesmo  InvalidResetTokenException  e  o  mesmo  401 (RN-F0.3-01 / RN-F0.3-02).

Resposta 401 não  distingue  se  o  token  "nunca  foi  válido", "expirou" ou  "já  foi  usado"  - anti-enumeração (CA-07: "mensagem NÃO revela se o token foi 'já usado' ou apenas 'inválido'").

Passo  8:  WebApp substitui o formulário pelo DS/EmptyState com botão "Solicitar novo link" → /recuperar-senha (F0.2).

Lacunas:  ver  nota  de  lacuna  de  especificação  no  cabeçalho  -  se  validação  pre-mount  for necessária, mapear GET /auth/validate-reset-token no OpenAPI.

## F0.3-c - 422 senha reutilizada

Escopo: erro 422 - nova senha é igual a uma das 3 últimas Atores: Usuário, WebApp, AuthController, ResetPasswordUseCase, Postgres

Pré-condições: token JWT válido (assinatura, audience, exp ok; JTI não consumido)

<!-- image -->

## Notas:

Passo 6: Argon2id.compare itera as últimas 3 entradas de passwordHistory; basta um MATCH para lançar PasswordReuseException (RN-F0.3-06).

A  validação  de  histórico  é  estritamente  backend  -  o  frontend  não  tem  acesso  aos  hashes anteriores.

Passo 9: campo "Nova senha" é limpo pelo WebApp; corpo RFC 7807 inclui detail: "Esta senha já foi utilizada recentemente." (RN-F0.3-05 + CA-04).

O JTI não é inserido em blacklist neste fluxo - o token permanece válido para nova tentativa com senha diferente.

## HU 4. US-F0-004 - Visualizar Informações de Contato da Secretaria

COMO um visitante ou usuário do sistema QUERO acessar a página de contato da secretaria PARA

obter o endereço, telefone, e-mail e horário de atendimento sem precisar fazer login. DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO Critério de contexto:

## Critério 1: Exibição dos dados de contato

DADO QUE um visitante acessa /contato em desktop QUANDO a página carrega ENTÃO exibe card com: - ícone MapPin + endereço completo da secretaria - ícone Phone + número(s) de telefone como links tel: - ícone Mail + e-mail institucional como link mailto:

- ícone Clock + horário de atendimento

ao lado (coluna direita) exibe placeholder do mapa com label de localização

## Critério 2: Layout responsivo

DADO QUE um visitante acessa /contato QUANDO a viewport é ≥ 1024px ENTÃO exibe grid de 2 colunas: informações | mapa QUANDO a viewport é &lt; 1024px ENTÃO exibe layout de 1 coluna empilhada: informações acima, mapa abaixo

## Critério 3: Link de retorno ao login

DADO QUE o visitante está na página /contato QUANDO clica em "Voltar ao login" ENTÃO é redirecionado para /login (F0.1)

## Critério 4: Acessibilidade

DADO QUE o visitante usa leitor de tela ENTÃO o mapa possui atributo alt descrevendo a localização da secretaria os links de telefone têm aria-label legível (ex: "Ligar para (41) 3361-XXXX")

COMO

qualquer usuário do sistema (autenticado ou não)

QUERO

ver uma mensagem de erro clara e amigável quando ocorrer uma falha (acesso negado, recurso não encontrado ou erro interno)

PARA

entender o que aconteceu, obter um ID de incidente para suporte e saber como prosseguir sem ficar preso em uma tela genérica.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

## HU 5. US-F0-005 - Exibir Página de Erro Amigável

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

## Critério 1: Erro 401 (não autenticado)

um usuário não autenticado tenta acessar uma rota protegida

DADO QUE QUANDO o middleware detecta ausência de JWT válido ENTÃO

o sistema redireciona para /erro/401 a tela exibe: ícone cadeado, código "401" em destaque, mensagem amigável de sessão expirada exibe botões: "Fazer login" (primary → /login) e "Ir ao início" (secondary → /) a paleta de fundo é neutra (surface/default)

## Critério 2: Erro 403 (sem permissão)

DADO QUE um usuário autenticado tenta acessar recurso além de sua autorização QUANDO o backend retorna 403 Forbidden ENTÃO o sistema exibe /erro/403 a tela exibe: ícone cadeado, código "403", mensagem amigável de permissão negada exibe botões: "Ir ao início" (primary → /inicio) e "Ir ao suporte" (secondary) a paleta de fundo é "danger subtle" (surface/danger/subtle)

## Critério 3: Erro 404 (recurso não encontrado)

DADO QUE o usuário navega para uma URL inexistente no frontend OU o backend retorna 404 Not Found para uma chamada de API QUANDO o roteador captura a rota desconhecida ou o interceptor captura a resposta 404 ENTÃO o sistema redireciona para /erro/404 a tela exibe: ícone arquivo riscado, código "404", mensagem amigável exibe botões: "Ir ao início" e "Ir ao suporte" a paleta é neutra

## Critério 4: Erro 500 (erro interno)

DADO QUE

o backend retorna 5xx em qualquer chamada de API QUANDO o interceptor do TanStack Query captura o erro ENTÃO o sistema redireciona para /erro/500 a tela exibe: ícone raio, código "500", mensagem amigável de erro interno exibe o ID de incidente: "ID do incidente: INC-2026-XXXX" exibe botões: "Ir ao início" e "Ir ao suporte" a paleta é "warning" (surface/warning/subtle)

## Critério 5: Exibição do ID de incidente

DADO QUE o backend retorna erro 5xx com campo "instance" no corpo RFC 7807 QUANDO a tela de erro 500 é exibida ENTÃO o ID de incidente no campo "instance" é exibido abaixo da mensagem principal o ID pode ser copiado pelo usuário para acionar o suporte

## Critério 6: Ausência de stack trace / dados técnicos

DADO QUE qualquer erro ocorre no sistema QUANDO a tela de erro é exibida ao usuário ENTÃO NENHUM stack trace, nome de classe, query SQL ou detalhe técnico interno é visível a mensagem é sempre em linguagem natural e amigável

## Critério 7: Acessibilidade

DADO QUE

a  ilustração  da  tela  de  erro  possui  alt  text  descritivo  (ex:  "Ícone  de  cadeado  indicando  acesso negado")

um usuário utiliza leitor de tela ENTÃO o código de erro e a mensagem são anunciados em sequência lógica de leitura

## DIAGRAMAS DE SEQUÊNCIA

## F0.5-a - TanStack Query interceptor: API 5xx → /erro/500 com incidentId

Escopo: sequência - interceptor global captura resposta 5xx e extrai incidentId do corpo RFC 7807 Atores: Usuário, WebApp, TanStack Query, API (qualquer endpoint) Pré-condições: usuário autenticado; qualquer chamada useQuery ou useMutation ativa

<!-- image -->

Notas: Passo 4: corpo RFC 7807 - o campo instance carrega o incidentId no formato INC-YYYY-XXXX gerado pelo backend em cada erro 5xx (RN-F0.5-05 / CA-05). Passo  5:  o  interceptor  global  do  TanStack  Query  (QueryClient.defaultOptions.queries.onError) extrai instance antes de lançar para o componente; o incidentId é passado via navigate('/erro/500', { state: { incidentId } }) (RN-F0.5-09). O incidentId é exibido na tela /erro/500 como texto copiável - permite acionar o suporte sem expor stack trace (CA-06). Este  diagrama  é  transversal  -  aplica-se  a  todos  os  módulos  (solicitações,  formativas,  presença,

etc.) quando o backend retorna 5xx.

## F0.5-b - TanStack Query interceptor: API 4xx → /erro/:codigo

Escopo: sequência - interceptor global captura respostas 401 (sessão expirada), 403 e 404 vindas da API Atores: Usuário, WebApp, TanStack Query, API (qualquer endpoint) Pré-condições:  usuário  autenticado  com  JWT  presente; o token pode estar expirado (401) ou o recurso pode ser inacessível (403/404)

<!-- image -->

Notas: Passo  4  -  401  (sessão  expirada):  JWT  expirou  durante  a  sessão;  o  backend  rejeita  o  request. Diferente do CA-01 (sem JWT), onde o guard frontend bloqueia antes de fazer qualquer request (NAO\_APLICAVEL  acima).  Ver  F0.1-f  (refresh  token)  -  o  interceptor  tenta  refresh  antes  de redirecionar para /erro/401 (fluxo de renovação implícito). Passo 4 - 403: usuário autenticado sem capability para o recurso (FGAC); o backend retorna 403 com  type:  .../access\_denied.  O  interceptor  captura  e  redireciona  para  /erro/403  (CA-02  / RN-F0.5-07). Passo 4 - 404: recurso não encontrado via chamada de API (ex.: /requests/:id com ID inexistente). Diferente do CA-03 router (NAO\_APLICAVEL), que captura rotas inexistentes no frontend (CA-03 / RN-F0.5-07). Os  botões  de  ação  da  tela  /erro/:codigo  (RN-F0.5-06..07)  são  determinados  pelo  código  HTTP recebido - lógica de componente React sem round-trip. Lacunas: o fluxo de tentativa de refresh antes do redirect 401 está implícito (ver F0.1-f); se esse comportamento for formalizado em US-F1-002 ou em um hook dedicado (useAuthRefresh), este diagrama deve ser expandido ou referenciado.

## HU 6. US-F0-006 - Verificar Autenticidade de Protocolo PDF

COMO um terceiro (empregador, instituição, banca avaliadora) que recebeu um protocolo de solicitação em PDF QUERO verificar  a  autenticidade  desse  protocolo  informando  seu  número  ou  escaneando  o QR Code do documento PARA confirmar que o documento foi emitido pelo sistema da UFPR SEPT e que seu conteúdo não foi adulterado.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

## Critério 1: Protocolo encontrado (resultado OK, sem upload)

DADO QUE um verificador acessa /publico/verificar-protocolo/PROT-2026-00123 QUANDO a página carrega e o backend retorna 200 OK ENTÃO exibe DS/Card com: - Número do protocolo: PROT-2026-00123 - Tipo de solicitação (ex.: "Aproveitamento de Disciplina") - Status com DS/ProtocolBadge (ex.: "CONCLUÍDA", variante success)

- Data de criação e data de conclusão

- Hash SHA-256 truncado em fonte mono (expandível) abaixo do card exibe zona de drag-and-drop com instrução de upload opcional exibe caption explicativa sobre o propósito da verificação anti-fraude

## Critério 2: Verificação de integridade por upload (hash confere)

DADO QUE o protocolo foi carregado com sucesso (CA-01) QUANDO o verificador arrasta o PDF original para a zona de upload OU clica na zona e seleciona o arquivo via dialog ENTÃO o browser calcula SHA-256 do arquivo localmente (sem enviar ao servidor) compara com o hash retornado pelo backend ao coincidir exibe DS/AlertBanner variante "success": "Hash confere. O documento é autêntico e não foi modificado." exibe ícone CheckCircle ao lado da zona de upload

Critério 3: Verificação de integridade por upload (hash não confere)

DADO QUE o protocolo foi carregado com sucesso QUANDO o verificador faz upload de um PDF com conteúdo diferente do original OU um PDF adulterado ENTÃO o browser detecta divergência de hash exibe DS/AlertBanner variante "danger": "Atenção: o hash deste arquivo NÃO confere com o protocolo registrado. O documento pode ter sido adulterado." NENHUMA informação adicional é enviada ao servidor

Critério 4: Protocolo não encontrado DADO QUE QUANDO o backend retorna 404 Not Found ENTÃO

um verificador acessa /publico/verificar-protocolo/PROT-INEXISTENTE exibe DS/EmptyState com: - ícone de documento com ponto de interrogação - título: "Protocolo não encontrado" - descrição: "O número informado não corresponde a nenhum protocolo registrado." NÃO há zona de upload (nada a verificar) Critério 5: Loading skeleton DADO QUE o verificador acessa a página e a API está em processamento QUANDO a resposta ainda não chegou ENTÃO exibe DS/Skeleton cobrindo a área do card de resultado a zona de upload não está visível durante o loading Critério 6: Rate limit atingido DADO QUE o mesmo IP realizou mais de 10 requisições em 1 minuto para este endpoint QUANDO o limite é excedido ENTÃO o backend retorna 429 Too Many Requests a tela exibe DS/AlertBanner warning: "Muitas verificações realizadas. Aguarde antes de tentar novamente."

Critério 7: Acessibilidade da zona de upload

DADO QUE o verificador usa apenas teclado ou leitor de tela ENTÃO

a zona de drag-and-drop possui botão alternativo "Selecionar arquivo" ativável via Tab o resultado da verificação (confere / não confere) é anunciado via aria-live="assertive"

## DIAGRAMAS DE SEQUÊNCIA

F0.6-a - Verificar protocolo (protocolo encontrado)

Escopo: happy path - GET retorna metadados sanitizados Atores: Verificador, WebApp, ProtocoloController, VerificarProtocoloUseCase, Postgres

Pré-condições: endpoint público, sem JWT; ID de protocolo válido e existente; &lt; 10 req/min do IP

<!-- image -->

Notas: Passo  2:  endpoint  totalmente  público  -  nenhum  filtro  JWT  é  aplicado;  RateLimitFilter  verifica apenas IP (RN-F0.6-01). Passo 6: solicitanteNomeParcial é mascarado no UseCase (ex.: "A S**") antes de sair da camada de aplicação - nunca expõe nome completo ou GRR (RN-F0.6-02 / LGPD). Passo 6: o campo hashSha256 (64 chars) é retornado completo na resposta JSON; o truncamento visual (RN-F0.6-03) ocorre no componente React. O  endpoint  não  expõe  a  qual  usuário  pertence  o  protocolo  -  confirma  apenas  existência  e

metadados do documento (RN-F0.6-09).

F0.6-b - Verificação de integridade por upload (SHA-256 local)

Escopo: sequência client-side - hash calculado no browser sem envio ao servidor Atores: Verificador, WebApp (FileDropzone + Web Crypto API)

Pré-condições: CA-01 já executado; hashSha256 esperado em memória no componente React

<!-- image -->

## Notas:

Passo 2: fileBuffer é lido via FileReader.readAsArrayBuffer(file) no browser; o arquivo nunca sai do dispositivo do verificador (RN-F0.6-04 / RN-F0.6-06).

Passo 3: SubtleCrypto.digest é assíncrono (Promise); o componente exibe DS/Skeleton transitório durante o cálculo (CA-05, NAO\_APLICAVEL como sequência separada). alt  branch  else:  o  banner  danger instrui o verificador a não confiar no documento e a contatar a

secretaria (CA-03).

Zero mensagens HTTP neste diagrama - a privacidade é preservada estruturalmente: o servidor só fornece o hash esperado (F0.6-a), nunca processa o PDF enviado (RN-F0.6-05 / RN-F0.6-06).

F0.6-c - Protocolo não encontrado (404)

Escopo: erro 404 - ID informado não corresponde a nenhum protocolo registrado Atores: Verificador, WebApp, ProtocoloController, VerificarProtocoloUseCase, Postgres Pré-condições: endpoint público, &lt; 10 req/min; ID não existe na base

<!-- image -->

Notas: Passo 7: resposta RFC 7807 - type: .../errors/not-found, detail: "Nenhum protocolo registrado com este identificador.". Zona de upload (FileDropzone) não é renderizada para 404 - não há hash esperado com o qual comparar (CA-04).

F0.6-d - Rate limit atingido (429) Escopo: erro 429 - proteção contra enumeração de IDs de protocolo Atores: Verificador, WebApp, RateLimitFilter Pré-condições: mesmo IP realizou ≥ 10 requisições ao endpoint em &lt; 1 minuto

## Notas:

Passo 3: RateLimitFilter rejeita antes do ProtocoloController; o VerificarProtocoloUseCase nunca é invocado (RN-F0.6-07).

Tentativas que excedam  o threshold configurável (ex.: 100 req/5min) geram  métrica  no Prometheus → alerta no Grafana para suspeita de bruteforce (RN-F0.6-08 - configuração de ops, fora do fluxo de usuário).

## HU 7. US-F0-007 - Verificar Autenticidade de Certificado Digital

## COMO

um  terceiro  (empregador,  banca,  institution)  que  recebeu  um  certificado  de  participação  ou atividade emitido pelo sistema

QUERO

verificar a autenticidade do certificado informando o hash SHA-256 (impresso no QR Code do PDF) PARA

confirmar que ele foi emitido pelo sistema oficial da UFPR SEPT, que os dados não foram alterados e que a assinatura digital é criptograficamente válida.

<!-- image -->

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Certificado válido (fluxo principal)

DADO QUE um verificador acessa /publico/verificar-certificado/&lt;hash\_válido&gt; QUANDO o backend retorna 200 OK com metadados + assinatura ED25519 o browser busca a chave pública em /.well-known/jwks.json a verificação criptográfica da assinatura ED25519 é bem-sucedida ENTÃO exibe DS/Badge grande variante "success": "Certificado válido" exibe card com: - Nome do beneficiário (completo ou parcial, conforme configuração) - Evento / atividade - Carga horária - Data de emissão exibe bloco "Assinatura digital": - Ícone Shield - Texto: "Assinado digitalmente pelo servidor da UFPR SEPT"

exibe DS/VerificationSeal (64px, variante "success", ícone ShieldCheck) - Link "Ver chave pública" → /.well-known/jwks.json Critério 2: Certificado inválido (hash não encontrado ou assinatura falha) DADO QUE um verificador acessa a URL com hash inexistente no sistema OU com hash válido mas a assinatura ED25519 não confere com a chave pública QUANDO o browser tenta verificar ENTÃO exibe DS/VerificationSeal variante "danger" (ícone ShieldX ou ShieldAlert) exibe DS/Badge grande variante "danger": "Certificado inválido" exibe mensagem: "Este certificado não pôde ser verificado. Ele pode ser falso ou ter sido adulterado." NÃO exibe dados do beneficiário Critério 3: Certificado revogado / expirado DADO QUE (ex.: atividade cancelada administrativamente após emissão) QUANDO o verificador acessa a URL do certificado ENTÃO o backend retorna status "REVOGADO" nos metadados exibe DS/Badge variante "warning": "Certificado revogado" exibe mensagem explicativa sobre a revogação DADO QUE o certificado foi verificado com sucesso QUANDO ENTÃO

o certificado foi emitido mas posteriormente revogado pelo sistema AINDA exibe os dados básicos para identificação histórica Critério 4: Verificação de integridade por upload do PDF (opcional) DADO QUE o certificado foi encontrado e validado (CA-01) QUANDO o verificador faz upload do PDF recebido na zona drag-and-drop ENTÃO o browser calcula SHA-256 do arquivo localmente compara com o hash do certificado retornado pelo backend se coincidir: exibe DS/AlertBanner success "O arquivo confere com o certificado registrado." se divergir: exibe DS/AlertBanner danger "O arquivo NÃO confere com o certificado original." Critério 5: Link para chave pública JWKS o verificador clica em "Ver chave pública"

é aberta a URL /.well-known/jwks.json em nova aba

o JSON retornado contém a chave pública ED25519 no formato JWK Critério 6: Loading e estados intermediários DADO QUE o verificador acessa a URL e aguarda resposta QUANDO a API e a busca do JWKS estão em processamento ENTÃO exibe DS/Skeleton cobrindo a área do card de resultado o selo de verificação exibe estado "carregando" (spinner ou shimmer) Critério 7: Rate limit atingido DADO QUE o mesmo IP realizou mais de 10 requisições em 1 minuto QUANDO o limite é excedido ENTÃO o backend retorna 429 a  tela  exibe  DS/AlertBanner  warning:  "Muitas  verificações  realizadas.  Aguarde  antes  de  tentar novamente." Critério 8: Acessibilidade o status do certificado (válido/inválido/revogado) é anunciado via aria-live="assertive"

DADO QUE o verificador usa leitor de tela ENTÃO o selo de verificação possui alt text descritivo todos os elementos interativos são atingíveis via Tab DIAGRAMAS DE SEQUÊNCIA F0.7-a - Certificado válido (happy path: GET + JWKS + ED25519.verify) Escopo: happy path - hash encontrado, assinatura ED25519 válida, status=VALIDO Atores:  Verificador,  WebApp, Web Crypto API, CertificadoController, VerificarCertificadoUseCase, Postgres Pré-condições: endpoint público, sem JWT; hash SHA-256 válido e existente na base; &lt; 10 req/min do IP

<!-- image -->

Notas: Passo 2: endpoint público, sem JWT; RateLimitFilter aplica Bucket4j 10 req/min por IP (RN-F0.7-10) - o filtro fica antes do CertificadoController. Passo  6:  beneficiarioNome  pode ser completo ou mascarado conforme configuração do tipo de certificado (RN-F0.7-08). GRR e e-mail nunca são retornados (RN-F0.7-11). Passo 8: WebApp extrai jwksUrl do payload do passo 7 e faz um GET separado; a chave pública pode ser cacheada pelo browser via Cache-Control (RN-F0.7-06). Passos  10-11:  SubtleCrypto.verify  executa  inteiramente  no  browser  -  zero  bytes  do  PDF  ou  da assinatura voltam ao servidor (RN-F0.7-05). O certificado foi  gerado  pela  emissão  documentada em transversal/10.4-certificado-emissao.md (10.4a).

F0.7-b - Certificado revogado (status=REVOGADO)

Escopo: sequência - hash encontrado, assinatura original válida, mas status=REVOGADO Atores:  Verificador,  WebApp, Web Crypto API, CertificadoController, VerificarCertificadoUseCase,

Postgres

Pré-condições:  certificado  foi  emitido  e  posteriormente  revogado  pelo  sistema  (ex.:  atividade cancelada)

<!-- image -->

## Notas:

Passo  5:  status=REVOGADO  é  definido  no  backend  quando  a  atividade/evento  é  cancelado administrativamente após a emissão do certificado (RN-F0.7-07).

Passos 8-11:  a  assinatura  ED25519  ainda  é  verificada  mesmo  para  REVOGADO - confirma que o certificado  foi  genuinamente  emitido  pelo  sistema  antes  da  revogação  (não  é  um  documento forjado).

Passo  12:  diferente  do  INVÁLIDO  (F0.7-d),  o  REVOGADO  exibe  dados  históricos  básicos  para identificação - quem foi o beneficiário, qual atividade, data de emissão (CA-03).

## F0.7-c - 404 hash não encontrado

Escopo: erro 404 - hash SHA-256 não corresponde a nenhum certificado registrado Atores: Verificador, WebApp, CertificadoController, VerificarCertificadoUseCase, Postgres Pré-condições: hash inexistente na base de dados (certificado nunca emitido ou hash adulterado)

<!-- image -->

Notas:

Passo 8: nenhum dado do beneficiário é exibido - não há certificado para identificar; apenas o selo de inválido e a mensagem genérica de CA-02 (RN-F0.7-09).

Nenhuma chamada ao /.well-known/jwks.json é feita - sem payload de assinatura para verificar.

F0.7-d - Assinatura ED25519 inválida (tampering client-side)

Escopo: erro - hash encontrado no backend (200), mas SubtleCrypto.verify retorna false Atores:  Verificador,  WebApp, Web Crypto API, CertificadoController, VerificarCertificadoUseCase, Postgres

Pré-condições: hash existe na base; assinatura no payload é forjada ou o response foi interceptado (MITM)

<!-- image -->

Notas:

Este diagrama é o argumento de segurança central: o backend pode retornar 200, mas a chave pública em /.well-known/jwks.json é a fonte da verdade independente - qualquer assinatura não gerada pela chave privada do servidor falha no SubtleCrypto.verify (RN-F0.7-05 / RN-F0.7-06). Cenários reais que levam ao false: (1) DB comprometido com assinatura substituída; (2) MITM no response  da  API  modificando assinaturaEd25519; (3) PDF forjado cujo hash coincide por colisão (SHA-256 - improvável) mas assinatura é inválida. Passo 12: nenhum dado do beneficiário é exibido (CA-02: "NÃO exibe dados do beneficiário"). A  separação  entre  F0.7-c  (404  -  hash  não  encontrado)  e  F0.7-d  (200  +  verify  fail)  documenta explicitamente  que  a  validação  tem  duas  camadas  independentes:  disponibilidade  no  DB  e integridade criptográfica.

- 8.2 ALUNO

## HU 8. US-F1-001 - Dashboard do Aluno (Visão Unificada)

COMO um aluno autenticado QUERO ver um painel unificado ao entrar no sistema PARA ter  visão  imediata  de  minhas  pendências,  horas  formativas,  solicitações  em  aberto,  próximos eventos e atalhos para as principais funcionalidades. DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Carregamento inicial com dados (fluxo principal)

DADO QUE o aluno está autenticado com mustChangePassword = false QUANDO navega para /inicio ENTÃO enquanto a resposta chega exibe DS/Skeleton em cada bloco (4 retângulos para KpiRow, 3 linhas para cada lista)

o sistema realiza GET /bff/dashboard/aluno com o JWT do aluno ao receber resposta 200 OK renderiza: - Saudação: "Olá, {nome}" + Caption com curso e período - KpiRow: 4 DS/KpiCards (horas formativas, solicitações, eventos hoje, certificados) - Seção Pendências com até 3 itens e CTAs HATEOAS - Tabela "Últimas solicitações" com 5 linhas - Seção "Próximos eventos" com 3 cards - Coluna direita: Prazos, Último parecer, QuickTiles

## Critério 2: KpiCard de horas formativas

DADO QUE o aluno tem 72 horas validadas de um total de 120 requeridas QUANDO o dashboard carrega ENTÃO exibe DS/KpiCard com valor "72 / 120 h" e barra de progresso a 60% a cor da barra usa token brand/primary Critério 3: Pendências com CTA HATEOAS

DADO QUE o aluno tem uma solicitação no estado EM\_AJUSTE o BFF retorna \_links.pendencias[0].href com o link de edição QUANDO o dashboard renderiza ENTÃO exibe o item de pendência com DS/Badge estado "EM AJUSTE" botão CTA vinculado ao href do \_links correspondente se \_links.pendencias estiver ausente ou vazio, exibe DS/EmptyState na seção

## Critério 4: SLA breach na tabela de solicitações

DADO QUE uma solicitação tem prazo\_em anterior a now QUANDO a tabela "Últimas solicitações" é renderizada ENTÃO a célula de prazo exibe a data em cor status/danger sem tooltip explicativo de "SLA vencido"

## Critério 5: Degradação graciosa (erro parcial de módulo)

DADO QUE o serviço de solicitações está indisponível Mas os demais módulos (formativas, eventos, certificados) respondem normalmente QUANDO o BFF agrega os dados ENTÃO a seção "Últimas solicitações" exibe DS/AlertBanner warning: "Não foi possível carregar as solicitações no momento."

as demais seções renderizam normalmente o aluno não perde acesso ao resto do dashboard

## Critério 6: Estado vazio (aluno recém-cadastrado)

DADO QUE o aluno não tem solicitações, pendências ou eventos QUANDO o dashboard carrega ENTÃO cada seção vazia exibe DS/EmptyState com mensagem contextual: - Pendências: "Nenhuma pendência no momento." - Solicitações: "Você ainda não abriu solicitações." - Eventos: "Nenhum evento próximo."

## Critério 7: Pull-to-refresh mobile

DADO QUE o aluno está no dashboard em dispositivo mobile QUANDO puxa a tela para baixo (pull-to-refresh) ENTÃO o indicador nativo de refresh aparece no topo o TanStack Query invalida o cache de /bff/dashboard/aluno os dados são rebuscados e atualizados na tela

## Critério 8: Responsividade

DADO QUE o aluno acessa o dashboard QUANDO a viewport é ≥ 1280px ENTÃO

KpiRow exibe 4 colunas e o layout principal usa proporção 2:1 QUANDO a viewport é 768-1023px ENTÃO KpiRow exibe 2x2 e o layout empilha (coluna esquerda acima da direita) QUANDO a viewport é &lt; 768px ENTÃO KpiRow exibe 2x2 ou com scroll horizontal a sidebar fica em overlay (drawer)

## DIAGRAMAS DE SEQUÊNCIA

## F1.1-D01 - Carregamento inicial do dashboard (happy path - cache MISS)

Escopo: happy path - primeiro acesso ou cache Redis expirado Atores: Aluno, WebApp, JwtFilter, DashboardBFF, Redis, Postgres Pré-condições: aluno autenticado (mustChangePassword  =  false),  access  token  válido  com dashboard.view\_own Notas: Passo 8: o BFF executa as 5 queries de forma paralela (async/await Promise.all ou coroutines); falha isolada em um bloco ativa degradação graciosa (ver F1.1-D03). Passo  11:  useActions(\_links)  filtra  botões  disponíveis;  botão  "Nova  solicitação"  só  aparece  se \_links.novaSolicitacao estiver presente - aluno sem solicitacao.create não recebe o link. Passo 12: skeleton (DS/Skeleton) exibido entre os passos 2-11; substituído bloco a bloco conforme dados chegam.

<!-- image -->

## F1.1-D02 - Carregamento do dashboard (cache HIT - FCP &lt; 1,5 s)

Escopo: retorno em cache Redis dentro da janela de 30 s (RN-F1.1-10) Atores: Aluno, WebApp, JwtFilter, DashboardBFF, Redis Pré-condições: cache Redis populado há menos de 30 s para o alunoId Notas: O cache Redis não é invalidado por pull-to-refresh do mobile (ver F1.1-D04); o TanStack Query do cliente invalida somente seu cache local. Dados servidos pelo BFF permanecem frescos até o TTL expirar. Dados cobertos pelo cache: kpis, pendencias, ultimasSolicitacoes, proximosEventos. O cache não cobre dados tempo-real como janelaAberta de eventos - esses são buscados diretamente quando o aluno abre a tela do evento.

<!-- image -->

## F1.1-D03 - Degradação graciosa (módulo de solicitações indisponível)

Escopo: erro parcial de módulo - CA-05, RN-F1.1-01 Atores: Aluno, WebApp, JwtFilter, DashboardBFF, SolicitacoesQuery, FormativasQuery Pré-condições:  módulo  de  solicitações  lança  timeout  ou  503;  demais  módulos  respondem normalmente Notas: Passo  9:  o  BFF  retorna  HTTP  200  mesmo  com  módulo  parcialmente  degradado;  o  frontend interpreta  solicitacoes:  null  como  sinal  para  exibir o DS/AlertBanner. Módulos que responderam normalmente são renderizados sem degradação. Passo  10:  DS/AlertBanner  exibe  "Não  foi  possível  carregar  as  solicitações  no  momento."  - conforme CA-05. O aluno conserva acesso a todas as demais seções. O BFF usa try/catch por módulo dentro do agregador; a falha de um bloco não cancela os demais.

<!-- image -->

## F1.1-D04 - Pull-to-refresh no mobile (CA-07, RN-F1.1-11)

Escopo: pull-to-refresh reinvalida cache TanStack Query e rebusca dados Atores: Aluno, MobileApp, TanStackQuery, DashboardBFF, Redis, Postgres Pré-condições: aluno autenticado no app mobile, dashboard já carregado Notas: Passos 6-9: se o cache Redis ainda estiver válido (TTL &gt; 0), o BFF serve do cache sem ir ao Postgres - o pull-to-refresh do mobile invalida apenas o cache TanStack Query (client-side), não o Redis (server-side). Dados são garantidamente frescos dentro da janela de 30 s. Passo 3: indicador nativo (RefreshControl no React Native) aparece imediatamente ao gesto; some ao completar o passo 11. Para forçar invalidação do Redis também (ex.: aluno quer ver solicitação recém-aberta), o futuro endpoint pode aceitar Cache-Control: no-cache header - não previsto no MVP.

<!-- image -->

## HU 9. US-F1-002 - Primeiro Acesso: Definir Senha e Aceitar LGPD

COMO um aluno que está acessando o sistema pela primeira vez (ou após reset administrativo) QUERO definir minha senha pessoal e confirmar o aceite da política de privacidade (LGPD) PARA

desbloquear o acesso ao sistema e garantir que meus dados são tratados com meu consentimento informado.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

Critério 1: Tela exibida corretamente após login com mustChangePassword DADO QUE QUANDO é redirecionado para /primeiro-acesso ENTÃO

o aluno fez login e recebeu mustChangePassword = true (US-F0-001 CA-02) vê o card centralizado com: - Ícone Shield + título "Primeiro acesso" - Texto explicativo sobre a necessidade de definir senha - Campo "Nova senha" com medidor de força e toggle de visibilidade - Campo "Confirmar senha" - Checkbox LGPD com link para a política de privacidade - Botão "Continuar" desabilitado até os requisitos serem cumpridos

a sidebar não tem links de navegação ativados

## Critério 2: Botão habilitado somente com todos os requisitos cumpridos

DADO QUE o aluno está em /primeiro-acesso QUANDO preenche os campos de senha com senha forte válida os dois campos coincidem marca o checkbox de aceite LGPD ENTÃO o botão "Continuar" fica habilitado (variant=primary) QUANDO desmarca o checkbox de LGPD ENTÃO o botão "Continuar" volta a ficar desabilitado

## Critério 3: Validação de senha (frontend em tempo real)

DADO QUE o aluno digita no campo "Nova senha"

ENTÃO o medidor de força (DS/Progress 4 segmentos) atualiza em tempo real cada requisito da lista (12 chars, maiúscula, minúscula, número, especial) exibe ✓ ao ser cumprido DADO QUE o aluno preenche "Confirmar senha" diferente da nova senha clica em "Continuar" ENTÃO exibe mensagem inline "As senhas não coincidem" sem realizar chamada à API

## Critério 4: Conclusão bem-sucedida

DADO QUE o aluno preencheu senha forte com confirmação igual e marcou o checkbox LGPD QUANDO clica em "Continuar" ENTÃO o sistema realiza POST /auth/first-access { novaSenha, aceiteTermos: true }

ao receber 200 OK: - senha\_alterada = true no backend - aceite\_lgpd\_em = now() registrado com IP e User-Agent - evento iam.first\_access\_completed emitido no audit\_log redireciona para /inicio (US-F1-001) a partir deste momento todos os links da sidebar estão habilitados

## Critério 5: Tentativa de navegar para outra rota durante o bloqueio

DADO QUE o aluno tem mustChangePassword = true e está em /primeiro-acesso QUANDO tenta navegar para /inicio ou qualquer rota protegida (digitando URL diretamente) ENTÃO é redirecionado de volta para /primeiro-acesso

- a mensagem na tela permanece intacta

## Critério 6: Acessibilidade

DADO QUE o aluno usa apenas teclado para navegar ENTÃO a  ordem  de  tab  é:  nova  senha → confirmar  senha → checkbox LGPD → link  política → botão Continuar erros de validação são anunciados via aria-live="assertive" o checkbox possui label clicável com área de toque de 44px

## DIAGRAMAS DE SEQUÊNCIA F1.2-D01 - Conclusão bem-sucedida do primeiro acesso (happy path)

Escopo: happy path - aluno define senha forte, aceita LGPD, backend persiste e redireciona Atores: Aluno, WebApp, JwtFilter, FirstAccessController, FirstAccessUseCase, Postgres Pré-condições: aluno autenticado com mustChangePassword = true; senha forte não reutilizada; checkbox LGPD marcado Notas: Passo 7: a verificação  novaSenha  ≠  senha\_hash\_temp  ocorre  em  memória  no  UseCase (Argon2id.verify)  logo  após  receber  a  linha  do  Postgres  -  sem  nova  roundtrip.  Se  a  verificação falhar, dispara F1.2-D03. Passo 9: INSERT audit\_log + COMMIT são atômicos com o UPDATE do passo 8 (mesma transação). Se o sistema tiver outbox de boas-vindas configurado, um INSERT outbox\_event(iam.first\_access\_completed) entra neste mesmo COMMIT; dispatch via transversal/10.1-outbox-notificacao.md.

<!-- image -->

Passo 11: mustChangePassword é limpo do estado local (React Context / Zustand); a sidebar passa a renderizar todos os links normalmente. Nenhum novo JWT é emitido - o guard é verificado via estado derivado da store, não de claim JWT.

## F1.2-D02 - Bloqueio de navegação (mustChangePassword guard)

Escopo: CA-05 · RN-F1.2-01 - tentativa de acessar rota protegida com mustChangePassword = true Atores: Aluno, WebApp Pré-condições: aluno autenticado; mustChangePassword = true na store do cliente; não concluiu o fluxo

<!-- image -->

## Notas:

Passo 2: o guard é implementado como componente &lt;RequireFirstAccess&gt; (React Router v6) que lê o  flag  mustChangePassword do contexto de autenticação - nenhuma chamada HTTP ocorre. O bloqueio é imediato, client-side.

Chamadas  diretas à API (ex.: GET  /bff/dashboard/aluno)  com  um  JWT  de  usuário  com senha\_alterada=false  podem  ser  protegidas  adicionalmente  no  backend  por  um  filtro  Spring Security que verifica a coluna senha\_alterada - a implementação desse filtro está no escopo do módulo IAM mas não requer diagrama separado (sem bifurcação de mensagens aqui).

A  sidebar  permanece  sem links enquanto mustChangePassword = true (RN-F1.2-02), reforçando visualmente o bloqueio.

## F1.2-D03 - 422 - Senha igual à senha temporária (RN-F1.2-05)

Escopo: erro - nova senha é idêntica à senha temporária emitida pelo sistema Atores: Aluno, WebApp, JwtFilter, FirstAccessController, FirstAccessUseCase, Postgres Pré-condições:  aluno  autenticado  com  mustChangePassword  =  true;  fornece  a  própria  senha temporária como nova senha

<!-- image -->

Notas: Passo  8:  o  UseCase  executa  Argon2id.verify(novaSenha,  senha\_hash\_temp) em memória após o SELECT  -  se  retornar  true  (senhas  iguais),  lança  SenhaReutilizadaException,  a  transação  faz ROLLBACK e o controller retorna 422. Passo 9: resposta segue RFC 7807 Problem Details: status=422, type="urn:secretaria:error:senha\_reutilizada",  detail="A  nova  senha  não  pode  ser  igual  à  senha temporária gerada pelo sistema.". O detail não revela o hash ou a senha.

O frontend não expõe o motivo da rejeição além da mensagem acima - não há dica de qual era a senha temporária.

## HU 10. US-F1-003 - Gerenciar Perfil, Segurança e Notificações

Esta US cobre 3 sub-funcionalidades agrupadas no mesmo épico de autogestão.

## HU-A - Editar dados pessoais (F1.3)

COMO aluno autenticado QUERO editar meus dados pessoais (nome social, telefone, e-mail de contato, foto de perfil) PARA manter minhas informações atualizadas para comunicações da secretaria.

## HU-B - Trocar senha e gerenciar sessões (F1.4)

COMO aluno autenticado QUERO trocar minha senha e visualizar e encerrar sessões ativas em outros dispositivos PARA manter minha conta segura.

## HU-C - Configurar preferências de notificação (F1.5)

COMO aluno autenticado QUERO

configurar quais canais (e-mail, push, in-app) recebo para cada tipo de notificação, definir horário de DND e escolher entre entrega imediata ou digest

PARA

não ser inundado de notificações e receber o que é importante no canal certo.

## DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

## Critério 1: Editar dados pessoais

DADO QUE o aluno está em /perfil QUANDO altera o campo "Telefone" para um novo valor ENTÃO o botão "Salvar" fica habilitado (dirty state) ao clicar em "Salvar" o sistema realiza PATCH /me { telefone: "..." } exibe DS/Toast success: "Perfil atualizado com sucesso." ao clicar em "Cancelar" antes de salvar, os valores voltam ao estado original sem chamada à API

## Critério 2: Upload de foto de perfil

DADO QUE o aluno está em /perfil QUANDO clica em "Alterar foto" e seleciona uma imagem JPEG de 1 MB ENTÃO abre modal de crop circular para ajustar a imagem ao confirmar, a imagem é enviada para MinIO via URL pré-assinada o DS/Avatar é atualizado com a nova foto se a imagem exceder 2 MB, exibe erro: "A imagem deve ter no máximo 2 MB."

## Critério 3: Trocar senha

DADO QUE o aluno está em /perfil/seguranca QUANDO preenche "Senha atual" corretamente e "Nova senha" + "Confirmar nova senha" com senha forte clica em "Salvar senha" ENTÃO o sistema realiza PATCH /me/password { senhaAtual: "...", novaSenha: "..." } ao receber 200 OK: todas as outras sessões são invalidadas exibe DS/AlertBanner success: "Senha alterada. Outras sessões foram encerradas."

QUANDO preenche "Senha atual" incorretamente ENTÃO recebe 401 e exibe erro inline: "Senha atual incorreta."

## Critério 4: Encerrar sessão de outro dispositivo

DADO QUE o aluno está em /perfil/seguranca a lista de sessões exibe uma sessão de "iPhone - Safari" com \_links.encerrar QUANDO clica em "Encerrar" para esta sessão ENTÃO o sistema realiza DELETE /me/sessions/{sessionId} a sessão é removida da lista o dispositivo correspondente perde acesso (refresh token inválido) a sessão atual NÃO aparece com botão de encerramento

## Critério 5: Configurar notificações

DADO QUE o aluno está em /perfil/notificacoes QUANDO desabilita o switch de "push" para notificações de prioridade MEDIUM clica em "Salvar preferências" ENTÃO o sistema realiza PATCH /me/notifications com a nova configuração a partir deste ponto, notificações de prioridade MEDIUM não chegam por push QUANDO tenta desabilitar notificações CRITICAL ENTÃO os switches para canais CRITICAL estão bloqueados (disabled) e não podem ser alterados

## DIAGRAMAS DE SEQUÊNCIA

## F1.3-D01 - Editar dados pessoais (PATCH /me)

Escopo: happy path - aluno atualiza dado pessoal (ex.: telefone) e salva Atores: Aluno, WebApp, JwtFilter, ProfileController, UpdateProfileUseCase, Postgres Pré-condições:  aluno em /perfil com formulário populado via GET /me; algum campo alterado (dirty state ativo)

<!-- image -->

Notas: Passo 6: somente os campos enviados no PATCH são atualizados (merge parcial); campos omitidos permanecem  intactos.  Campos somente-leitura (GRR, email institucional) são ignorados mesmo que presentes no body (UseCase filtra via allowlist). Passo 8: a resposta inclui o estado completo atualizado do usuario para o frontend sincronizar o formulário sem recarregar a página. Botão "Cancelar" (RN-F1.3-03): descarta as alterações locais sem chamar a API - não há diagrama dedicado.

## F1.3-D02 - Upload de foto de perfil (MinIO presigned URL)

Escopo: happy path - aluno seleciona imagem, obtém URL pré-assinada e envia diretamente para MinIO Atores: Aluno, WebApp, JwtFilter, ProfileController, MinIO, Postgres Pré-condições: imagem ≤ 2 MB, formato JPEG/PNG/WebP; crop circular confirmado no modal

<!-- image -->

Notas: Passo  8:  o  upload  vai  direto  do  browser  para  o  MinIO  -  o  backend  nunca  recebe  os  bytes  da imagem, evitando gargalo de banda e simplificando o handling de multipart. Passo  10:  a  segunda  chamada  ao  ProfileController  não  passa  pelo  JwtFilter  explicitamente  no diagrama para evitar segundo activate no mesmo participante; na implementação, o Bearer token é enviado e validado normalmente pelo filtro Spring Security.

Se a imagem exceder 2 MB, o frontend rejeita antes do passo 1 (validação client-side via File API) - nenhuma chamada HTTP é feita. Se o MinIO rejeitar por content-type inválido, o PUT retorna 403 e o frontend exibe DS/AlertBanner warning.

## F1.4-D03 - Trocar senha + invalidar outras sessões (happy path)

Escopo: happy path - CA-03 · RN-F1.4-05 - senha atual correta, nova senha forte, outras sessões invalidadas Atores: Aluno, WebApp, JwtFilter, ChangePasswordUseCase, Postgres Pré-condições: aluno em /perfil/seguranca; senha atual conhecida; nova senha forte e diferente

<!-- image -->

Notas: Passo 6: após receber senha\_hash, o UseCase executa Argon2id.verify(senhaAtual, senha\_hash) em memória. Se falhar → ROLLBACK + dispara F1.4-D04. Se confirmar → continua para o passo 7. Passo 8: invalida todos os refresh\_token exceto currentSessionId - o aluno permanece logado na sessão atual. Tokens de acesso já emitidos expiram naturalmente em até 15 min.

Requisitos de força da nova senha (RN-F1.4-02): mesma lógica do ResetPasswordUseCase - DRY → F0/US-F0-003-NOVA-SENHA.md F0.3-a.

## F1.4-D04 - 401 - Senha atual incorreta

Escopo: erro - CA-03 branch negativo · RN-F1.4-01 - Argon2id.verify falha Atores: Aluno, WebApp, JwtFilter, ChangePasswordUseCase, Postgres Pré-condições: aluno preenche senha atual errada; nova senha pode ser forte ou não

<!-- image -->

Notas: Passo 8: resposta segue RFC 7807 - status=401, type="urn:secretaria:error:senha\_atual\_incorreta". O backend não revela o hash nem fornece dica sobre a senha correta. Tentativas repetidas são limitadas pela mesma política de rate-limit do login (Bucket4j, 5 req/min por IP+usuarioId) - proteção contra brute force na troca de senha.

## F1.4-D05 - Listar sessões ativas + encerrar sessão de outro dispositivo (HATEOAS)

Escopo: CA-04 · RN-F1.4-03 · RN-F1.4-04 - load das sessões + DELETE via \_links.encerrar Atores: Aluno, WebApp, JwtFilter, SessionsController, Postgres

Pré-condições: aluno em /perfil/seguranca; há ao menos uma sessão em outro dispositivo

<!-- image -->

Notas: Passo 7: a sessão atual não recebe \_links.encerrar (RN-F1.4-04) - o controller compara session.id ==  currentSessionId e omite o link. A UI, consequentemente, não exibe o botão "Encerrar" para a sessão atual. Passo 11: a cláusula AND id != currentSessionId no DELETE é um guard de segurança no backend - a sessão atual nunca é apagada mesmo se o sessionId for manipulado pelo cliente. Após encerramento, o refresh\_token do dispositivo alvo fica inválido; o próximo refresh tentado

por aquele dispositivo retorna 401, forçando novo login.

## F1.5-D06 - Salvar preferências de notificação (PATCH /me/notifications)

Escopo:  CA-05  ·  RN-F1.5-01  ·  RN-F1.5-03  ·  RN-F1.5-04  -  aluno  ajusta  matriz  de  canais,  DND  e digest Atores: Aluno, WebApp, JwtFilter, NotifPrefsController, NotifPrefsUseCase, Postgres Pré-condições: aluno em /perfil/notificacoes com preferências carregadas; altera ao menos um switch não-CRITICAL

<!-- image -->

Notas: Passo  6:  o  UseCase  revalida  que  nenhuma  preferência  CRITICAL foi  desabilitada  (mesmo  que  o frontend já bloqueie os switches, o backend não confia no cliente). Se a validação falhar → 422 Problem  Details.  A  lógica  de  DND  (RN-F1.5-03)  e  digest  (RN-F1.5-04)  são  colunas  do  mesmo registro UPSERT - sem fluxo adicional.

O notif\_prefs é uma tabela JSONB por usuário; o OutboxDispatcher lê essas preferências a cada entrega  de  notificação  (ver  transversal/10.1-outbox-notificacao.md).  A  mudança  de  preferências tem efeito imediato na próxima mensagem despachada.

Carregamento inicial da matriz (GET /me/notifications): mesmo padrão JWT-guarded do F1.3-D01 - precondição implícita do diagrama; não duplicado.

## HU 11. US-F1-004 - Visualizar e Gerenciar Comunicações Recebidas

COMO aluno autenticado QUERO ver todas as comunicações recebidas (avisos institucionais, mensagens da turma e inbox de ações) em um hub unificado com filtros PARA não perder comunicados importantes e agir quando uma mensagem requer minha atenção. DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

## CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

## Critério 1: Listagem de comunicações

DADO QUE o aluno está em /comunicacao QUANDO a página carrega ENTÃO exibe as tabs: Todos | Institucional | Turma | Inbox (com badge de contagem) a tab "Todos" está selecionada por padrão lista  CommunicationRow  para  cada  comunicação:  ícone  de  tipo,  título,  data,  Badge  prioridade, unread dot para não lidas

## Critério 2: Marcar como lido

DADO QUE o aluno vê uma comunicação com unread dot QUANDO clica na comunicação para expandir ou navegar ao detalhe ENTÃO o sistema verifica se \_links.marcar-lido existe realiza POST /communications/:id/read o unread dot desaparece o badge de contagem do topbar decrementa

## Critério 3: Tab Inbox com CTA pulsante

DADO QUE existe uma comunicação que requer ação do aluno QUANDO o aluno acessa a tab Inbox ENTÃO vê o item com CTA visualmente destacado (pulsante/animado) ao clicar no CTA é direcionado para a ação correspondente (ex.: dar ciência de atendimento)

## Critério 4: Filtros

DADO QUE

o aluno está em /comunicacao QUANDO aplica filtro "não lido" + tipo "Institucional" ENTÃO o sistema realiza GET /communications?lido=false&amp;tipo=INSTITUCIONAL exibe apenas as comunicações que correspondem ao filtro se não houver resultados exibe DS/EmptyState: "Nenhuma comunicação encontrada."

## Critério 5: Tabs scrolláveis no mobile

DADO QUE o aluno acessa /comunicacao em viewport 375px ENTÃO as tabs são scrolláveis horizontalmente sem quebrar layout cada tab permanece interativa com área de toque ≥ 44px

## DIAGRAMAS DE SEQUÊNCIA

## F1.6-D01 - Listagem de comunicações (GET /communications)

Escopo: happy path - aluno acessa /comunicacao, recebe lista com tabs e badges de não lidos Atores: Aluno, WebApp, JwtFilter, CommunicationsController, Postgres Pré-condições:  aluno  autenticado  com  communication.read;  há  ao  menos  uma  comunicação entregue Notas: Passo  5:  o  backend  resolve  tabs  e  badges  em  uma  única  query  (subquery  COUNT(*)  WHERE read\_at IS NULL GROUP BY tipo); o frontend não filtra localmente (RN-F1.6-04). Passo  8:  useActions(\_links)  mapeia  \_links.marcar-lido  (presente  apenas  se  read\_at  IS  NULL)  e CTAs de ação da Inbox - o aluno só vê o botão CTA se o link existir. Badge topbar (RN-F1.6-06): TanStack Query usa refetchInterval: 60\_000 nesta query - o contador se atualiza automaticamente a cada 60 s sem diagrama adicional. Skeleton (DS/Skeleton) exibido durante os passos 2-7; substituído pela lista ao receber 200.

<!-- image -->

## F1.6-D02 - Marcar comunicação como lida (POST /communications/:id/read)

Escopo: CA-02 · RN-F1.6-03 - clique em comunicação não lida dispara marcação  via \_links.marcar-lido Atores: Aluno, WebApp, JwtFilter, CommunicationsController, Postgres Pré-condições:  lista  carregada  (F1.6-D01);  comunicação  tem  read\_at  =  null  e  \_links.marcar-lido presente Notas: Passo 5: a cláusula AND destinatario\_id=:alunoId garante que um aluno não possa marcar como lida uma mensagem de outro - proteção IDOR no lado do banco. Se \_links.marcar-lido estiver ausente na resposta (ex.: mensagem já lida), o cliente não dispara o POST - a lógica de "marcar" é cega ao estado anterior; o servidor idempotentemente faz UPDATE sem erro mesmo se read\_at já estiver preenchido. Atualização otimista (UX): o unread dot pode ser removido imediatamente no cliente (optimistic update do TanStack Query) antes da confirmação do backend; em caso de erro 4xx/5xx o cliente reverte.

<!-- image -->

## F1.6-D03 - Filtros aplicados no backend (query params)

Escopo:  CA-04  ·  RN-F1.6-04  -  aluno  combina  filtros;  o  backend  filtra  via  query  params,  não  o frontend Atores: Aluno, WebApp, JwtFilter, CommunicationsController, Postgres Pré-condições: aluno em /comunicacao com lista já carregada; seleciona "Não lido" + "Institucional"

<!-- image -->

Notas: Passo 5: o backend aplica todos os filtros via WHERE composto - o frontend nunca filtra um array local. Isso garante paginação correta e performance com volume alto de mensagens (RN-F1.6-04). Múltiplos  filtros  combinados  (tipo  +  lido  +  tab)  são  todos  query  params  no  mesmo  GET  -  sem endpoint  separado  por  filtro.  A  paginação  (?page=0&amp;size=20)  é  somada  aos  filtros  na  mesma chamada. DS/EmptyState é renderizado quando communications: [] - sem chamada HTTP adicional.

## HU 12. US-F1-005 - Abrir, Listar e Acompanhar Solicitações

## HU-A - Listar minhas solicitações (F1.7)

COMO aluno autenticado QUERO ver todas as minhas solicitações com filtros por estado, tipo e ano PARA acompanhar o andamento de cada pedido e identificar quais precisam de ação minha.

## HU-B - Abrir nova solicitação via wizard dinâmico (F1.8)

COMO aluno autenticado QUERO abrir  qualquer  tipo  de  solicitação  seguindo  um  wizard  de  3  passos  com  formulário  gerado dinamicamente a partir do tipo escolhido PARA formalizar pedidos à secretaria ou colegiado de forma padronizada, com anexos e sem conhecer a burocracia interna do fluxo.

## HU-C - Visualizar detalhe e acompanhar timeline (F1.9)

COMO aluno autenticado QUERO ver os detalhes completos de uma solicitação, sua linha do tempo de eventos e as ações disponíveis PARA entender  exatamente  onde  está  meu  pedido  e  tomar  as  ações  necessárias  (corrigir,  gerar protocolo).

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

## Critério de contexto:

## Critério 1: Listar solicitações com filtros

DADO QUE o aluno está em /solicitacoes QUANDO a página carrega ENTÃO exibe tabela com colunas: Número, Tipo, Estado (DS/Badge), Prazo, SLA solicitações com prazo vencido têm a data em status/danger o botão "Nova solicitação" aparece apenas se \_links.novaSolicitacao existir QUANDO aplica filtro estado = "EM\_ANALISE" ENTÃO realiza GET /requests?solicitante=me&amp;estado=EM\_ANALISE exibe apenas as solicitações filtradas

## Critério 2: Passo 1 do wizard: escolha do tipo

DADO QUE o aluno está em /solicitacoes/nova QUANDO o wizard carrega ENTÃO exibe grid de cards com os tipos de solicitação elegíveis para o aluno tipos para os quais o aluno não tem elegibilidade NÃO aparecem na lista ao selecionar um tipo avança para o Passo 2

## Critério 3: Passo 2 do wizard: formulário dinâmico

DADO QUE o aluno selecionou um tipo de solicitação com form\_schema QUANDO

o Passo 2 carrega ENTÃO renderiza os campos definidos no form\_schema (texto, select, data, multi-item) campos condicionais aparecem/somem conforme valores preenchidos a zona de upload de anexos está disponível a validação Zod ocorre inline ao perder foco de cada campo

## Critério 4: Upload de anexo no wizard

DADO QUE o aluno está no Passo 2 do wizard QUANDO arrasta um arquivo PDF de 5 MB para a zona de upload ENTÃO o cliente calcula o SHA-256 do arquivo localmente inicia o upload para MinIO via URL pré-assinada exibe barra de progresso durante o upload ao concluir exibe o arquivo na lista com nome e botão de remover

QUANDO tenta fazer upload de arquivo acima de 10 MB ENTÃO exibe erro: "O arquivo excede o tamanho máximo de 10 MB."

## Critério 5: Passo 3: revisão e confirmação

DADO QUE o aluno completou os campos e anexos no Passo 2 QUANDO avança para o Passo 3 ENTÃO exibe resumo legível dos dados preenchidos (sem campos técnicos) lista os anexos com pré-visualização ou ícone de tipo botão "Confirmar" destravado ao clicar em "Confirmar" realiza POST /requests e redireciona para /solicitacoes/:id

## Critério 6: Rascunho salvo automaticamente

DADO QUE o aluno começou a preencher o Passo 2 do wizard QUANDO fecha o browser acidentalmente ou sai da tela ENTÃO ao retornar para /solicitacoes/nova o sistema detecta o rascunho salvo localmente oferece opção de "Continuar rascunho" ou "Começar novo"

## Critério 7: Detalhe com timeline e ações HATEOAS

DADO QUE o aluno acessa /solicitacoes/:id QUANDO a página carrega com estado EM\_AJUSTE ENTÃO exibe ActionBar com botão "Editar" (derivado de \_links.editar) a timeline exibe todos os request\_events em ordem reversa ao clicar em "Editar" reabre o wizard no Passo 2 com dados pré-preenchidos

QUANDO o estado é DELIBERADA e \_links.gerar-protocolo existe ENTÃO exibe botão "Gerar protocolo" na ActionBar

## DIAGRAMAS DE SEQUÊNCIA F1.7-D01 - Listar solicitações paginadas (GET /requests?solicitante=me)

Escopo: happy path - aluno acessa /solicitacoes e vê suas solicitações com filtros opcionais Atores: Aluno, WebApp, JwtFilter, RequestsController, Postgres Pré-condições: aluno autenticado com request.view\_own

<!-- image -->

Notas: Passo 8: \_links.novaSolicitacao aparece somente se o aluno tiver request.open - aluno sem essa authority não vê o botão, sem código condicional no frontend. Filtros  adicionais  (estado,  tipo,  ano,  busca  textual)  são  acrescentados  como  query  params  no mesmo GET (?estado=EM\_ANALISE&amp;ano=2026); o Postgres aplica a cláusula WHERE correspondente - sem filtragem client-side (RN-F1.7-01). Em  mobile  (RN-F1.7-03)  a  DS/DataTable  é  substituída  por  cards  e  o  painel  de  filtros  fica  em Sheet/drawer - mesmo fluxo HTTP, sem variação de mensagens.

## F1.8-D02 - Passo 1 do wizard: tipos de solicitação elegíveis (GET /request-types)

Escopo: Passo 1 - backend filtra tipos pelo curso, período e pré-requisitos do aluno Atores: Aluno, WebApp, JwtFilter, RequestTypesController, Postgres Pré-condições: aluno clicou "Nova solicitação" (via \_links.novaSolicitacao do D01)

<!-- image -->

Notas: Passo 5: a query avalia request\_type.prerequisitos (JSONB com regras de período mínimo, situação acadêmica,  etc.)  contra  os  dados  do  aluno  -  tipos  não  elegíveis  são  excluídos  da  query,  não ocultados no frontend (RN-F1.8-02). O aluno nunca vê nem recebe os tipos não elegíveis. O form\_schema retornado em cada tipo já vem nesta resposta para o Passo 2 poder renderizar o formulário sem nova chamada HTTP (cache local do TanStack Query).

## F1.8-D03 - Upload de anexo no wizard (SHA-256 + MinIO presigned PUT)

Escopo: CA-04 · RN-F1.8-04 - aluno envia arquivo PDF ao MinIO diretamente via URL pré-assinada

Atores: Aluno, WebApp, JwtFilter, AttachmentController, MinIO Pré-condições: aluno no Passo 2 do wizard; arquivo ≤ 10 MB, tipo PDF/JPEG/PNG

<!-- image -->

## Notas:

Passo  1:  o  SHA-256  é  calculado  no  browser  via  crypto.subtle.digest('SHA-256',  buffer)  antes  de qualquer  upload  -  sem  chamada  HTTP.  O  backend  recebe  o  hash  para  validar  integridade pós-upload (comparação opcional via MinIO.statObject).

O  fileKey  ficará  na  memória  do  wizard  (Zustand  /  React  state)  e  será  enviado  no  array attachmentKeys[] do POST /requests (D04).

Se o arquivo exceder 10 MB, a rejeição ocorre no passo 1 (File API no cliente) - sem chamada HTTP.

F1.8-D04 - Confirmar wizard: POST /requests (criar solicitação + workflow + outbox)

Escopo: CA-05 · RN-F1.8-06 · RN-F1.8-07 - Passo 3 confirmado; backend abre solicitação, calcula prazo, emite evento Atores: Aluno, WebApp, JwtFilter, OpenRequestController, OpenRequestUseCase, Postgres Pré-condições: Passos 1 e 2 completos; form\_schema validado; anexos enviados ao MinIO

<!-- image -->

Notas: Passo  6:  o  UseCase  re-valida  dados  contra  o  form\_schema  do  RequestType no backend - não confia  apenas  na  validação  Zod  do  frontend  (RN-F1.8-03).  Campos  ausentes  ou  com  formato inválido retornam 422 Problem Details. Passo  7:  numero\_anual  usa  sequência  PostgreSQL  atômica  (YYYY-{nextval()})  para  garantir unicidade  sem  colisão  em  concorrência.  O  prazo\_em  é  calculado  como  NOW()  +  INTERVAL '&lt;prazo\_dias&gt; days'. Passo  8:  o  outbox\_event  é  inserido  na  mesma  transação  -  garante  que  a  notificação  (aluno  +

secretaria) só dispara após o commit. Dispatch via transversal/10.1-outbox-notificacao.md.

F1.8-D05 - Salvar rascunho no backend (POST /requests/draft)

Escopo: CA-06 · RN-F1.8-05 - aluno sai do wizard; dados parciais são persistidos no backend como RASCUNHO

Atores: Aluno, WebApp, JwtFilter, OpenRequestController, Postgres Pré-condições: aluno está no Passo 2 com dados parciais; fecha aba ou navega para outra rota

<!-- image -->

Notas: O  rascunho  também  é  salvo  localmente  via  PWA/AsyncStorage  (conforme  RN-F1.8-05)  para recuperação offline; o backend é a fonte da verdade persistente. Ao retornar para /solicitacoes/nova, o frontend verifica localStorage/AsyncStorage: se encontrar draftId, exibe modal "Continuar rascunho ou começar  novo?" -  decisão exclusivamente client-side, sem nova chamada HTTP. O  rascunho  aparece  em  /solicitacoes  (F1.7-D01)  com  badge  "Rascunho"  -  estado=RASCUNHO retornado no GET /requests.

F1.9-D06 - Detalhe da solicitação com timeline e \_links HATEOAS

Escopo: CA-07 · RN-F1.9-01 · RN-F1.9-04 - GET /requests/{id} retorna dados completos, timeline reversa e ações disponíveis Atores: Aluno, WebApp, JwtFilter, RequestsController, Postgres Pré-condições: aluno autenticado com request.view\_own; solicitação existe e pertence ao aluno Notas: Passo 3: solicitante\_id = alunoId é verificado no próprio JwtFilter ou no controller antes de atingir o Postgres - proteção IDOR: aluno não consegue ver solicitações de outros. Passo  6:  request\_event  retorna  em  ORDER  BY created\_at DESC - timeline em ordem reversa (RN-F1.9-04). O mais recente aparece no topo. Passo 8: a ActionBar é 100% derivada de \_links (RN-F1.9-01). Estado EM\_AJUSTE → \_links.editar; estado  DELIBERADA → \_links.gerar-protocolo.  A  UI  não  tem  if  (estado  ==  'EM\_AJUSTE') hardcoded.

<!-- image -->

F1.9-D07 - Gerar protocolo PDF com QR (POST /requests/{id}/protocol)

Escopo: RN-F1.9-03 - aluno clica "Gerar protocolo" (via \_links.gerar-protocolo); backend gera PDF e retorna URL de download Atores: Aluno, WebApp, JwtFilter, ProtocolUseCase, Postgres, MinIO Pré-condições: solicitação no estado DELIBERADA; \_links.gerar-protocolo presente em D06

<!-- image -->

Notas: Passo 7: o PDF é gerado internamente (template engine ou Gotenberg headless) e contém: número da solicitação, dados do formulário, timeline de decisão e QR-code apontando para /publico/verificar-protocolo/:id.  A  geração  é  síncrona  nesta  versão  MVP  (&lt;  2s  tipicamente);  se ultrapassar 5s, converter para job assíncrono com polling. O  QR  no  PDF  aponta  para  a  rota  pública  de  verificação  (US-F0-006)  -  sem  autenticação necessária. downloadUrl é uma presigned GET URL do MinIO com TTL=15min; o browser inicia o download diretamente sem trafegar bytes pelo backend.

F1.9-D08 - Download de anexo via presigned GET URL (MinIO, TTL=15 min)

Escopo: RN-F1.9-05 - aluno baixa um anexo da solicitação sem trafegar bytes pelo backend Atores: Aluno, WebApp, JwtFilter, AttachmentController, MinIO Pré-condições: solicitação carregada (D06); anexo presente na DS/AttachmentList Notas: TTL=15  min  (RN-F1.9-05):  curto  o  suficiente  para  dificultar  compartilhamento  indevido,  longo  o suficiente para o browser iniciar o download sem erro de expiração. Diferença vs D03 (upload): aqui é presigned GET (download) com TTL=15 min vs presigned PUT (upload) com TTL=5 min. O fileKey vem do attachment.file\_key retornado em D06. O owner check do passo 3 garante que um aluno não possa gerar URL de download para anexo de solicitação alheia - proteção IDOR por requestId + attachId.

<!-- image -->

## HU 13. US-F1-006 - Submeter e Acompanhar Atividades Formativas

COMO aluno autenticado QUERO

submeter  comprovantes  de  atividades  formativas  complementares  e  acompanhar  o  parecer da CAAF PARA registrar minhas horas e obter o certificado correspondente quando aprovadas.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto:

Critério 1: Listar formativas DADO QUE o aluno está em /formativas QUANDO a página carrega ENTÃO filtrável por estado e tipo exibe tabela com atividades, horas, estado (DS/Badge) e data button "Nova atividade" visível se \_links.nova existir Critério 2: Submeter nova formativa manualmente DADO QUE o aluno está em /formativas/nova QUANDO seleciona tipo de atividade, informa carga horária e faz upload de comprovante PDF clica em "Enviar" ENTÃO o sistema realiza POST /formative-entries a entrada aparece na lista com estado "SUBMETIDA" a CAAF do curso recebe notificação via Outbox Critério 3: Confirmar formativa pré-validada (evento interno) DADO QUE o aluno participou de um evento interno com presença validada o sistema criou automaticamente uma formative\_entry PENDENTE\_CONFIRMACAO QUANDO o aluno acessa /formativas/nova (ou link direto) ENTÃO o formulário está pré-preenchido com os dados do evento (campos readonly) exibe DS/AlertBanner info: "Participação registrada pelo sistema. Confirme para finalizar." ao clicar em "Confirmar" transiciona para SUBMETIDA sem necessidade de upload Critério 4: Detalhe com link para certificado DADO QUE a formativa do aluno tem estado APROVADA \_links.baixar-certificado existe na resposta QUANDO o aluno acessa /formativas/:id ENTÃO exibe badge "APROVADA" em success exibe botão "Baixar certificado" na ActionBar ao clicar realiza o download do PDF do certificado emitido DIAGRAMAS DE SEQUÊNCIA

F1.10-D01 - Listar formativas do aluno (GET /formative-entries)

Escopo: happy path - aluno acessa /formativas e vê todas suas entradas com filtros Atores: Aluno, WebApp, JwtFilter, FormativeController, Postgres Pré-condições: aluno autenticado com formative.view\_own Notas: Passo 8: \_links.nova aparece somente se formative.submit estiver nas authorities do aluno - sem condicional hardcoded no frontend. Filtros por estado e tipo são query params no mesmo GET (?estado=SUBMETIDA&amp;tipo=CURSODOTACAO); o Postgres  aplica  WHERE  composto  -  sem filtragem client-side (analogia com RN-F1.7-01 de solicitações). Entradas no estado PENDENTE\_CONFIRMACAO (pré-validadas por evento interno) aparecem na lista com badge "Aguardando confirmação" e CTA para F1.11-D03.

<!-- image -->

F1.11-D02 - Submeter formativa manualmente (POST /formative-entries)

Escopo:  CA-02  ·  RN-F1.11-01  ·  RN-F1.11-03  -  aluno  seleciona  atividade,  declara  horas,  anexa comprovante e submete Atores: Aluno, WebApp, JwtFilter, FormativeController, SubmitFormativeUseCase, Postgres Pré-condições: comprovante já enviado ao MinIO (via F1.8-D03 - presigned PUT); comprovanteKey disponível

<!-- image -->

## Notas:

Passo 6: a query valida que formative\_activity é elegível para o cursoId do aluno (RN-F1.11-01) - mesma  lógica  de  filtro  de  elegibilidade  do  backend.  Se  a  atividade  não  for  aplicável,  o  UseCase retorna 422 Problem Details.

Upload do comprovante (RN-F1.11-02): ocorre antes do passo 1, via presigned PUT ao MinIO - DRY → F1/US-F1-005-SOLICITACOES.md  F1.8-D03.  O  comprovanteKey  é  passado  aqui  sem  nova chamada de upload.

Passo 8: outbox\_event é inserido na mesma transação - a notificação à CAAF (formativas.submitted) só dispara após o COMMIT. Dispatch: transversal/10.1-outbox-notificacao.md.

F1.11-D03 - Confirmar formativa pré-validada por evento interno Escopo:  CA-03  ·  RN-F1.11-04  -  aluno  confirma  (1  clique)  atividade  pré-preenchida  pelo  sistema após presença validada Pré-condições: formative\_entry no estado PENDENTE\_CONFIRMACAO criada automaticamente Atores: Aluno, WebApp, JwtFilter, FormativeController, ConfirmFormativeUseCase, Postgres ao encerrar evento; aluno recebeu notificação Notas:

<!-- image -->

Passo  5:  a  cláusula  AND  estado=PENDENTE\_CONFIRMACAO  é  um  guard:  se  o  aluno  tentar confirmar uma entrada já SUBMETIDA ou em outro estado, o controller retorna 409 Conflict. Passo 9: a segunda chamada HTTP (POST .../confirm) passa pelo JwtFilter real na implementação; o  diagrama omite o self-call para respeitar a regra de "no máximo 1 self-call por diagrama" - o Bearer token e formative.submit são validados normalmente. Nenhum  upload  de  comprovante  é  necessário  neste  caminho  (RN-F1.11-04)  -  a  presença  foi

validada pelo sistema de eventos (US-F1-009), eliminando fraude por upload manual.

F1.12-D04 - Detalhe da formativa aprovada com \_links HATEOAS

Escopo:  CA-04  ·  RN-F1.12-01  ·  RN-F1.12-02  -  GET  /formative-entries/{id}  retorna  detalhe completo e link para certificado

Atores: Aluno, WebApp, JwtFilter, FormativeController, Postgres Pré-condições: aluno autenticado com formative.view\_own; entrada no estado APROVADA

<!-- image -->

## Notas:

Passo 7: \_links.baixar-certificado aparece somente quando estado=APROVADA e o certificado já foi emitido (trigger background: transversal/10.4-certificado-emissao.md). Em estados intermediários o link está ausente e o botão não é renderizado (HATEOAS FGAC).

Clique  em  "Baixar  certificado" → navegação  para  \_links.baixar-certificado.href → fluxo  de download coberto em F1/US-F1-010-CERTIFICADOS.md.

Estado  REJEITADA  +  \_links.resubmeter  (RN-F1.12-03):  quando  presente, o frontend exibe botão "Resubmeter". O fluxo de resubmissão é DRY → F1.11-D02 (mesmo POST /formative-entries com endpoint /resubmit).

## HU 14. US-F1-007 - Acompanhar Estágios e Enviar Documentos

## COMO

aluno autenticado realizando estágio curricular

## QUERO

visualizar  meus  estágios registrados, acompanhar a situação de cada um e enviar os documentos exigidos (TCE, relatórios)

## PARA

manter meu processo de estágio atualizado e receber pareceres do orientador/COE sem precisar ir presencialmente à secretaria.

## DESENHO DA(S) TELA(S)

<!-- image -->

## [INSERIR TELA MOBILE AQUI]

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto: Critério 1: Listar estágios DADO QUE o aluno está em /estagios QUANDO a página carrega ENTÃO exibe tabela com: Empresa, Supervisor, Vigência, Situação filtrável por situação (Ativo, Concluído, Pendente) se não há estágios: DS/EmptyState "Você não possui estágios registrados." Critério 2: Enviar documento de estágio DADO QUE o aluno está em /estagios/:id, tab Documentos o tipo "Relatório Final" tem \_links.upload na resposta QUANDO faz upload do PDF do relatório ENTÃO o documento é enviado para MinIO via URL pré-assinada o status do documento muda para "Enviado - aguardando parecer" o orientador/COE recebe notificação via Outbox (estagios.document\_uploaded) Critério 3: Visualizar pareceres DADO QUE o aluno está em /estagios/:id, tab Pareceres QUANDO há pareceres registrados pelo orientador/COE

ENTÃO

exibe cada parecer com: data, autor, texto e ícone de status (aprovado/rejeitado) os pareceres estão em ordem cronológica (mais recente no topo)

DIAGRAMAS DE SEQUÊNCIA

F1.13-D01 - Listar estágios do aluno (GET /internships?aluno=me)

Escopo: CA-01 · RN-F1.13-01 - happy path - aluno vê tabela de estágios registrados pela secretaria Atores: Aluno, WebApp, JwtFilter, InternshipsController, Postgres Pré-condições: aluno autenticado com internship.view\_own; ao menos um estágio registrado pela secretaria

<!-- image -->

Notas: Se  internships:  [],  a  UI  exibe  DS/EmptyState  "Você  não  possui  estágios  registrados."  -  sem diagrama separado (mesmo fluxo HTTP, payload diferente).

Filtros  por  situação  (?situacao=ATIVO)  são  query  params  no  mesmo  GET  -  sem  filtragem client-side. O  aluno  não  possui  botão  "Novo  estágio"  nesta  tela  (RN-F1.13-02):  a  ausência  de  \_links.novo  na resposta garante isso via HATEOAS.

F1.14-D02 - Detalhe do estágio: documentos + pareceres + \_links HATEOAS

Escopo: RN-F1.14-01 · RN-F1.14-02 · RN-F1.14-04 · CA-03 - GET /internships/{id} retorna dados de ambas as tabs em uma única resposta Atores: Aluno, WebApp, JwtFilter, InternshipsController, Postgres Pré-condições: aluno autenticado; estágio pertence ao aluno

<!-- image -->

Notas: Passo 5: uma única query (JOIN) retorna dados de ambas as tabs - tab switching é client-side sem nova chamada HTTP (RN-F1.14-01).

Passo 7: \_links.upload aparece por tipo de documento somente quando o documento ainda não foi entregue  ou  está  pendente  de  reenvio  (RN-F1.14-02).  A  UI  não  conhece  a  lógica  de  "quais documentos são obrigatórios" - depende exclusivamente dos links presentes. CA-03  (pareceres):  pareceres  vêm na mesma resposta, em ORDER BY created\_at DESC - mais recente no topo (RN-F1.14-04). Sem diagrama separado.

F1.14-D03 - Enviar documento de estágio (POST /internships/{id}/documents)

Escopo: CA-02 · RN-F1.14-02 · RN-F1.14-03 - aluno envia PDF via MinIO presigned URL e notifica orientador/COE via Outbox Atores: Aluno, WebApp, JwtFilter, InternshipsController, UploadDocumentUseCase, Postgres Pré-condições: \_links.upload presente para o tipo de documento (D02); arquivo enviado ao MinIO

(DRY → F1.8-D03); fileKey disponível

<!-- image -->

Notas:

Passo  1:  upload  do  PDF  ao  MinIO  ocorre  antes  desta  chamada  (presigned  PUT  -  DRY → F1/US-F1-005-SOLICITACOES.md F1.8-D03). O POST aqui registra apenas o fileKey, sem trafegar bytes pelo backend. Passo  7:  outbox\_event  é  inserido  na  mesma  transação  que  o  UPDATE  -  garante  que  o orientador/COE  só  é  notificado  após  confirmação  do  COMMIT. O dispatcher (transversal/10.1) entrega push/email ao orientador responsável. O  UseCase  verifica  que  \_links.upload  estava  presente  para  o  documentType  (validação  de HATEOAS no backend) - se o aluno tentar enviar um tipo não permitido, retorna 403 Problem Details.

## HU 15. US-F1-008 - Acompanhar TCC e Enviar Versão Final

COMO aluno em fase de conclusão de curso QUERO acompanhar o status do meu TCC, ver a composição da banca e as datas-chave, e fazer o upload da versão final PARA formalizar minha entrega e acompanhar o resultado da avaliação.

DESENHO DA(S) TELA(S)

<!-- image -->

[INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar TCCs DADO QUE o aluno está em /tccs QUANDO a página carrega ENTÃO

exibe tabela com: Título, Orientador, Situação (DS/Badge), Data defesa

se não há TCC cadastrado: DS/EmptyState "Nenhum TCC registrado. Consulte a secretaria." Critério 2: Fazer upload da versão final DADO QUE o aluno está em /tccs/:id \_links.upload-final existe na resposta (estado permite upload) QUANDO faz upload do arquivo PDF do TCC ENTÃO o arquivo é enviado para MinIO via URL pré-assinada o estado muda para "SUBMETIDO" a banca recebe notificação via Outbox (tcc.submitted) o botão de upload desaparece (estado não permite novo upload) Critério 3: Visualizar banca e datas DADO QUE o aluno está em /tccs/:id QUANDO a página carrega ENTÃO exibe composição da banca com nome e papel de cada membro exibe data prevista de defesa com destaque visual exibe data limite de entrega com badge danger se prazo próximo DIAGRAMAS DE SEQUÊNCIA F1.15-D01 - Listar TCCs do aluno (GET /tccs?aluno=me) Escopo: CA-01 · RN-F1.15-01 - happy path - aluno vê tabela de TCCs registrados pela secretaria

Atores: Aluno, WebApp, JwtFilter, TccController, Postgres Pré-condições: aluno autenticado com tcc.view\_own Notas: Se tccs:  [], a UI exibe DS/EmptyState "Nenhum TCC registrado. Consulte a secretaria." - mesmo fluxo HTTP, payload diferente. TCCs de períodos anteriores com situacao=CONCLUIDO aparecem na lista (RN-F1.15-02): o aluno pode ter histórico de TCCs, mas apenas um ATIVO por vez. O  aluno  não  possui  botão  "Novo  TCC":  a  abertura  é  feita  pela  secretaria  (F5)  e  a  ausência  de

<!-- image -->

\_links.novo na resposta garante isso via HATEOAS.

Escopo: CA-03 · RN-F1.16-01 · RN-F1.16-02 · RN-F1.16-04 - GET /tccs/{id} retorna tudo em uma

F1.16-D02 - Detalhe do TCC: equipe, banca, datas, timeline e \_links resposta Atores: Aluno, WebApp, JwtFilter, TccController, Postgres

Pré-condições: aluno autenticado com tcc.view\_own; TCC pertence ao aluno Notas: Passo 5: uma única query JOIN retorna equipe, banca, datas e timeline - tudo na mesma resposta (RN-F1.16-01). Sem round-trips adicionais. \_links.upload-final aparece somente quando o estado do TCC permite submissão (ex.: EM\_ELABORACAO,  CORREÇÕES\_SOLICITADAS)  -  HATEOAS  controla  a  visibilidade  do  botão (RN-F1.16-02). tccEvents com tipo=AVALIACAO e campo resultado (aprovado/reprovado/com\_correcoes) constrói a timeline de resultados da banca (RN-F1.16-04). Data limite com badge danger: computação client-side após receber datasChave.entrega - sem diagrama adicional.

<!-- image -->

F1.16-D03 - Enviar versão final do TCC (POST /tccs/{id}/upload)

Escopo: CA-02 · RN-F1.16-03 - aluno faz upload do PDF final e banca é notificada via Outbox Atores: Aluno, WebApp, JwtFilter, TccController, SubmitTccUseCase, Postgres Pré-condições:  \_links.upload-final presente (D02); arquivo enviado ao MinIO (DRY → F1.8-D03); fileKey disponível

<!-- image -->

Notas: Passo 1: upload do PDF ao MinIO ocorre antes desta chamada - DRY → F1/US-F1-005-SOLICITACOES.md  F1.8-D03  (presigned  PUT).  O  POST  aqui  registra  apenas  o fileKey. Passo 6: a cláusula AND aluno\_id=:alunoId é o guard IDOR - garante que nenhum aluno submeta versão ao TCC de outro. Passo  7:  INSERT  tcc\_event  +  INSERT  outbox\_event  + COMMIT são atômicos. O outbox\_event notifica todos os membros  da  banca  (bancaIds  obtidos  do  JOIN  em  passo  6).  Dispatch: transversal/10.1-outbox-notificacao.md. Passo 9: \_links retornado após submissão não contém mais upload-final - o botão desaparece sem lógica de estado hardcoded no frontend (CA-02 último critério).

## HU 16. US-F1-009 - Consultar Eventos e Confirmar Presença

COMO aluno autenticado QUERO ver  os  eventos  formativos  disponíveis  para  mim  e  confirmar  minha presença quando a janela de validação estiver aberta PARA registrar minha participação e acumular horas formativas de forma segura e verificável. DESENHO DA(S) TELA(S) [INSERIR TELA WEB AQUI] [INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar eventos disponíveis para o aluno DADO QUE o aluno está em /eventos QUANDO a página carrega ENTÃO exibe tabela com: Título, Período, Estado (badge Agendado/Em andamento/Concluído), Organizador, CH, Situação presença eventos com janela ativa exibem badge "Janela aberta" (success) na coluna de presença Critério 2: Modal de detalhe com AttendanceWidget (janela aberta)

```
DADO QUE o aluno clica em um evento com estado "Em andamento" _links.confirmar-presenca existe na resposta QUANDO o modal de detalhe abre ENTÃO exibe descrição do evento exibe AttendanceWidget com variante correspondente ao attendanceMode do evento exibe countdown da janela ativa Critério 3: Modal de detalhe sem widget (janela fechada) DADO QUE o aluno clica em um evento fora da janela de validação _links.confirmar-presenca NÃO existe na resposta QUANDO o modal de detalhe abre ENTÃO exibe apenas descrição e dados do evento NÃO exibe AttendanceWidget nem botão de confirmação NÃO há indicação de quando a janela abrirá (UI cega) Critério 4: Confirmação de presença (SECRET_SINGLE) DADO QUE o aluno está em /eventos/:id/presenca o modo é SECRET_SINGLE com janela ativa QUANDO informa o PIN correto no AttendanceWidget e clica em "Confirmar" ENTÃO o sistema realiza POST /events/:id/attendance/confirm { pin: "...", deviceUuid: "...", fase: "ENTRADA" }
```

ao receber 200 OK exibe DS/AlertBanner success: "Presença registrada com sucesso!" o ícone de presença na lista de eventos muda para "Completa"

Critério 5: Confirmação de presença (SECRET\_DUAL - fase entrada)

DADO QUE o aluno está em /eventos/:id/presenca o modo é SECRET\_DUAL e a janela de ENTRADA está ativa

QUANDO informa o PIN de entrada e confirma ENTÃO o sistema registra a fase de entrada exibe mensagem: "Entrada registrada. Confirme a saída quando solicitado." a situação de presença muda para "Parcial"

Critério 6: Janela expirada (countdown zerado)

DADO QUE QUANDO o countdown chega a 00:00

- o AttendanceWidget é bloqueado automaticamente sem necessidade de reload qualquer tentativa de confirmação retorna 403 do backend

o aluno está em /eventos/:id/presenca com countdown visível ENTÃO exibe DS/EmptyState: "A janela de validação encerrou."

Critério 7: Aluno não logado tentando confirmar presença

DADO QUE um aluno não autenticado acessa /eventos/:id/presenca QUANDO a página tenta carregar ENTÃO o sistema redireciona para /login após login bem-sucedido retorna para /eventos/:id/presenca automaticamente DIAGRAMAS DE SEQUÊNCIA F1.17-D01 - Listar eventos disponíveis para o aluno (GET /events?audience=me) Escopo: CA-01 · RN-F1.17-01 - happy path - aluno vê tabela de eventos com badge de estado e situação de presença Atores: Aluno, WebApp, JwtFilter, EventsController, Postgres Pré-condições: aluno autenticado com attendance.view\_open Notas: situacaoPresenca por evento:  PENDENTE  /  PARCIAL  (DUAL  fase  ENTRADA  concluída)  / COMPLETA.

<!-- image -->

Evento com janelaAtiva: true recebe badge adicional "Janela aberta" (success) na coluna situação. Se events: [], a UI exibe DS/EmptyState - mesmo fluxo HTTP, payload diferente.

F1.17-D02 - Modal de detalhe: GET /events/{id}/attendance/session

Escopo:  CA-02  ·  CA-03  ·  RN-F1.17-02  -  modal  retorna  detalhe  do  evento  +  session  info; \_links.confirmar-presenca condicional à janela ativa Atores: Aluno, WebApp, JwtFilter, AttendanceController, Postgres Pré-condições: aluno autenticado com attendance.view\_open

<!-- image -->

Notas: \_links.confirmar-presenca  aparece  somente  se  uma  janela de validação está ativa no momento (RN-F1.17-02). Sem a link, o modal não exibe widget nem botão de confirmação (CA-03).

attendanceMode determina a variante do DS/AttendanceWidget: SECRET\_SINGLE, SECRET\_DUAL, QR\_SINGLE, QR\_DUAL (RN-F1.17-03 - renderização client-side).

janelaExpira  (timestamp  UTC)  alimenta  o  DS/Countdown  client-side.  Sem round-trip adicional (RN-F1.18-07).

CA-03 (janela fechada) usa a mesma rota HTTP - apenas \_links.confirmar-presenca está ausente e janelaExpira é nulo.

F1.18-D03 - Confirmação de presença SECRET\_SINGLE (POST /events/{id}/attendance/confirm)

Escopo:  CA-04  ·  RN-F1.18-01  ·  RN-F1.18-06  ·  RN-F1.18-08  -  happy  path  -  aluno  informa  PIN  + deviceUuid, presença registrada atomicamente com outbox

Atores: Aluno, WebApp, JwtFilter, AttendanceController, ConfirmAttendanceUseCase, Postgres Pré-condições:  \_links.confirmar-presenca  presente  (D02);  modo  SECRET\_SINGLE;  janela  de validação ativa

<!-- image -->

Notas:

Passo 1: deviceUuid gerado uma vez no client (localStorage / Keychain mobile) e enviado em toda confirmação. Não muda por sessão de navegador/app.

Passo 8: ON CONFLICT (eventId, deviceUuid) é o guard de device binding (RN-F1.18-06). Em modo devicePolicy:  BIND,  o  INSERT  falha  se  outro  aluno  já  usou  o  mesmo  dispositivo  no  evento → ROLLBACK + HTTP 409.

Passo 8: validação do pin\_hash ocorre em memória no UseCase (BCrypt compare) antes de entrar na transação. Falha de PIN → 422 (não entra no BEGIN).

Passo 8: outbox\_event(presenca.confirmed) dispara geração de certificado/formativa assincronamente → transversal/10.1-outbox-notificacao.md (RN-F1.18-08).

Modos QR\_SINGLE e QR\_DUAL: mesmo endpoint POST /confirm, campo token substitui pin - DRY.

F1.18-D04 - Confirmação DUAL fase ENTRADA (SECRET\_DUAL / QR\_DUAL)

Escopo: CA-05 · RN-F1.18-02 - fase ENTRADA de modo dual → situacaoAluno: PARCIAL; outbox somente após SAÍDA

Atores: Aluno, WebApp, JwtFilter, AttendanceController, ConfirmAttendanceUseCase, Postgres Pré-condições: \_links.confirmar-presenca com fase: ENTRADA  presente (D02); janela de ENTRADA ativa; aluno sem registro de ENTRADA anterior

<!-- image -->

Notas:

Passo 7: se entrada\_existente não for nulo (aluno já registrou entrada), o UseCase rejeita com 409 - idempotência protegida.

Passo  8:  sem  outbox\_event  neste  passo  -  o  presenca.confirmed  só  é  emitido  após  a  SAÍDA completa (RN-F1.18-02). A fase SAÍDA usa o mesmo endpoint com fase: SAIDA. \_links.confirmar-saida retornado em passo 9 habilita o widget de SAÍDA quando a janela de saída abrir (useActions - sem lógica hardcoded no frontend). Inelegibilidade: se a janela de ENTRADA expirar antes de o aluno confirmar, a janela de SAÍDA não aparece  (\_links.confirmar-saida  ausente).  Sem diagrama adicional - backend simplesmente não inclui o link. Modos QR\_DUAL: mesmo fluxo, campo token substitui pin - DRY.

F1.18-D05 (ERRO) - Confirmação fora da janela de validação → 403

Escopo: CA-06 · RN-F1.18-05 - countdown zerou ou janela nunca abriu; backend retorna 403 sem revelar detalhes da política Atores: Aluno, WebApp, JwtFilter, AttendanceController, ConfirmAttendanceUseCase, Postgres Pré-condições: aluno autenticado; janela de validação expirada ou inativa no momento do POST

<!-- image -->

Notas: Passo 9: RFC 7807 Problem Details sem revelar hora de reabertura, configuração da janela ou PIN armazenado (RN-F1.18-05 - segurança).

Passo  10:  O  DS/AttendanceWidget  bloqueia  inputs  localmente  via  countdown  (RN-F1.18-07).  O backend  é  a  autoridade  final  -  o  403  é  a  segunda  linha  de  defesa  caso  o  countdown  seja manipulado client-side. Rate limiting: POST /events/{id}/attendance/confirm deve ter rate limiter (Bucket4j) por alunoId -  rejeições  excessivas  indicam  força-bruta  de  PIN.  Sem  diagrama  adicional  (configuração cross-cutting).

## HU 17. US-F1-010 - Visualizar e Baixar Certificados Emitidos

COMO aluno autenticado QUERO visualizar todos os meus certificados emitidos e baixar o PDF assinado digitalmente PARA

ter  acesso  aos  documentos que comprovam minhas atividades e participações, com garantia de autenticidade verificável por terceiros. DESENHO DA(S) TELA(S) [INSERIR TELA WEB AQUI] [INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar certificados DADO QUE

o aluno está em /certificados QUANDO a página carrega ENTÃO exibe tabela com: Tipo, Evento/Atividade, Data de emissão, botão Download filtrável por tipo e ano

se não há certificados: DS/EmptyState "Você ainda não possui certificados emitidos." Critério 2: Download do PDF DADO QUE o aluno clica em "Download" para um certificado \_links.download existe na resposta QUANDO o sistema gera a URL pré-assinada do MinIO ENTÃO o download do PDF inicia automaticamente o PDF contém o QR Code de verificação se a URL pré-assinada expirar (&gt; 15 min): novo clique gera nova URL Critério 3: Badge no dashboard ao receber novo certificado DADO QUE a CAAF aprovou uma formativa do aluno QUANDO o CertificateIssuerUseCase emite o certificado (processo background) ENTÃO o KpiCard de certificados no dashboard (US-F1-001) incrementa em 1 o aluno recebe notificação in-app + push: "Seu certificado de [atividade] foi emitido." o certificado aparece no topo da lista em /certificados DIAGRAMAS DE SEQUÊNCIA

F1.19-D01 - Listar certificados do aluno (GET /certificates?beneficiario=me)

Escopo: CA-01 · RN-F1.19-02 - happy path - aluno vê tabela de certificados emitidos, filtrável por tipo e ano Atores: Aluno, WebApp, JwtFilter, CertificateController, Postgres Pré-condições: aluno autenticado com certificate.view\_own

<!-- image -->

Notas: Filtros tipo e ano são opcionais - sem eles retorna todos os certificados do aluno. Se certificates: [], a UI exibe DS/EmptyState "Você ainda não possui certificados emitidos." \_links.download por certificado: rel canônico que aponta para GET /certificates/{id}/download-url. Frontend usa useActions por item para renderizar o botão. Certificados  são  somente-leitura  para  o  aluno:  nenhum  \_links.delete  ou  \_links.edit  existe  na resposta.

F1.19-D02 - Download do PDF via MinIO presigned URL

Escopo: CA-02 · RN-F1.19-03 - aluno clica "Download"; backend gera presigned GET URL de 15 min no MinIO Atores: Aluno, WebApp, JwtFilter, CertificateController, MinIO Pré-condições: \_links.download presente (D01); aluno autenticado com certificate.view\_own

<!-- image -->

Notas: Passo 3: certificate.aluno\_id = alunoId é o guard IDOR - aluno não baixa certificado de outro. Passo 5: presignedGetUrl gera URL temporária (15 min / 900s) assinada com credenciais MinIO. O PDF não é exposto publicamente por URL permanente. Se a URL expirar (&gt; 15 min desde o clique), novo clique em "Download" executa este mesmo fluxo e gera nova URL - conforme CA-02. O  PDF  retornado  pelo  MinIO  contém  QR  Code  com  URL  /publico/verificar-certificado/:hash (RN-F1.19-04 - DRY → F0/US-F0-007-VERIFICAR-CERTIFICADO.md).

F1.19-D03 - Emissão automática de certificado (CertificateIssuerUseCase - background)

Escopo: CA-03 · RN-F1.19-01 - processo background disparado por outbox\_event após presença confirmada ou formativa aprovada

Atores: OutboxScheduler, CertificateIssuerUseCase, Postgres, MinIO, NotificacaoDispatcher Pré-condições: outbox\_event com type IN ('presenca.confirmed', 'formativa.approved') e status=PENDING existe na tabela

<!-- image -->

## Notas:

Passo 4: self-call  agrupa  geração  de PDF, cálculo SHA-256 e assinatura ED25519 - operações em memória no servidor. O QR Code (/publico/verificar-certificado/:hash) é embutido no PDF neste passo.

Passo  7:  INSERT  certificate  +  INSERT  outbox\_event  +  UPDATE outbox\_event (PROCESSED) são atômicos.  Falha  no  MinIO  (passo  5-6)  interrompe  antes  do  BEGIN  -  o  outbox\_event  original permanece PENDING e será reprocessado no próximo ciclo (at-least-once delivery).

Passo  10:  notificacao\_in\_app  incrementa  o  KpiCard  de  certificados  no  Dashboard  (CA-03  / US-F1-001 TanStack Query invalida ao próximo poll ou SSE). O push FCM notifica: "Seu certificado de [atividade] foi emitido." (CA-03). Dispatch detalhado do outbox: transversal/10.1-outbox-notificacao.md.

## HU 18. US-F1-011 - Consultar Atendimentos e Dar Ciência

COMO aluno autenticado QUERO ver  os  atendimentos  que  a  secretaria  registrou  para  mim  e  confirmar  ciência  daqueles  que aguardam minha resposta PARA ter  registro  formal  dos  atendimentos  presenciais  e cumprir com as confirmações necessárias de forma digital. DESENHO DA(S) TELA(S) [INSERIR TELA WEB AQUI] [INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar atendimentos com pendência destacada DADO QUE o aluno está em /meus-atendimentos QUANDO a página carrega ENTÃO exibe tabela com: Data, Assunto, Status (DS/Badge), Ação atendimentos com estado PENDENTE\_CIENCIA têm badge "Pendente ciência" em warning exibem botão "Estou ciente" na coluna Ação Critério 2: Dar ciência DADO QUE o aluno vê atendimento com "Estou ciente" disponível \_links.acknowledge existe na resposta QUANDO clica em "Estou ciente" ENTÃO o sistema realiza POST /service-records/:id/acknowledge o estado muda para CIENCIA\_DADA o badge muda para "Ciente" (success) e o botão desaparece a pendência correspondente some do Dashboard o evento atendimentos.acknowledged é gravado em audit\_log com data/IP

Critério 3: Filtro por pendências

DADO QUE o aluno filtra por "Pendente ciência" ENTÃO exibe apenas atendimentos com estado PENDENTE\_CIENCIA se não há pendências: DS/EmptyState "Nenhum atendimento pendente."

DIAGRAMAS DE SEQUÊNCIA

F1.20-D01 - Listar atendimentos (GET /service-records?aluno=me)

Escopo: CA-01 · CA-03 · RN-F1.20-01 · RN-F1.20-02 - happy path - tabela de atendimentos com badge e filtro por status Atores: Aluno, WebApp, JwtFilter, ServiceRecordController, Postgres Pré-condições: aluno autenticado com service\_record.view\_own

<!-- image -->

Notas: CA-03  usa  o  mesmo  endpoint  com  status=PENDENTE\_CIENCIA:  se  resultado  [],  UI  exibe DS/EmptyState "Nenhum atendimento pendente." \_links.acknowledge por item: presente apenas quando status=PENDENTE\_CIENCIA - HATEOAS controla a renderização do botão sem lógica hardcoded no frontend. Atendimentos  registrados  pela  secretaria  (F5.13)  chegam  com  status=PENDENTE\_CIENCIA  por padrão (RN-F1.20-01). O aluno não possui \_links de criação ou contestação - somente visualização e ciência.

F1.20-D02 - Dar ciência (POST /service-records/{id}/acknowledge)

Escopo:  CA-02  ·  RN-F1.20-03  -  aluno  confirma  ciência;  transição  de  estado  +  registro  em audit\_log com IP

Atores: Aluno, WebApp, JwtFilter, ServiceRecordController, AcknowledgeUseCase, Postgres Pré-condições: \_links.acknowledge presente (D01); status=PENDENTE\_CIENCIA; aluno autenticado

<!-- image -->

## Notas:

Passo 6: AND status=PENDENTE\_CIENCIA na cláusula WHERE é o guard de idempotência - se o aluno tentar confirmar duas vezes, UPDATE  afeta 0 linhas. O UseCase deve verificar rowsAffected=0 e retornar 409 (already acknowledged).

Passo 6: AND aluno\_id=:alunoId é o guard IDOR - aluno não confirma atendimento de outro.

Passo  6:  ciencia\_ip  armazena  o  IP  para  rastreabilidade  legal  (RN-F1.20-03).  Extraído  do  header X-Forwarded-For (nginx reverse proxy) ou RemoteAddr como fallback.

INSERT  audit\_log  e  UPDATE  são  atômicos  (mesmo  BEGIN...COMMIT):  sem  audit\_log  sem transição, sem transição sem audit\_log.

Passo 8: \_links retornado sem acknowledge → frontend descarta botão via useActions. Pendência some do Dashboard no próximo poll/invalidação TanStack Query (RN-F1.20-05).

## 8.3 EGRESSO

## COMO

um ex-aluno (egresso) da UFPR SEPT que concluiu o curso

## QUERO

acessar um painel com meu histórico acadêmico, diploma, certificados e dados de colação PARA

ter  acesso  permanente  aos  meus  documentos  de  forma  segura,  podendo  reemiti-los  quando necessário sem precisar comparecer presencialmente à secretaria.

## DESENHO DA(S) TELA(S)

<!-- image -->

## HU 19. US-F2-001 - Dashboard do Egresso e Reemissão de Certificados

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

<!-- image -->

Critério de contexto: Critério 1: Acesso ao dashboard após transição para Egresso DADO QUE a secretaria registrou o diploma do aluno (F5.11) o sistema atribuiu role = EGRESSO ao usuário QUANDO o egresso faz login ENTÃO é redirecionado para /egresso/inicio (não para /inicio do aluno) o dashboard exibe dados read-only: curso concluído, data de conclusão, diploma, certificados NÃO exibe botões de criação (Nova solicitação, Nova formativa, etc.) Critério 2: Conteúdo do dashboard do egresso DADO QUE o egresso está em /egresso/inicio QUANDO a página carrega ENTÃO exibe KpiRow com indicadores históricos: - Total de horas formativas validadas (somente leitura) - Número de certificados emitidos - Situação do diploma (DS/Badge "Emitido" - success) exibe seção "Diploma": número, data de emissão, botão Download (se \_links.download) exibe seção "Certificados": lista dos certificados com botão Reemitir PDF (se \_links.reemitir) exibe seção "Dados de colação": data, turma (se disponível) todos os badges de status exibem variante "Concluído" (neutral ou success) Critério 3: Reemitir certificado DADO QUE o egresso está em /egresso/inicio um certificado tem \_links.reemitir na resposta QUANDO clica em "Reemitir PDF" ENTÃO o sistema regenera a URL pré-assinada do MinIO para o arquivo já existente o download do PDF inicia com o mesmo conteúdo, hash SHA-256 e assinatura ED25519 do original o certificado reemitido continua verificável em /publico/verificar-certificado/:hash NÃO é criado um novo registro de certificado na base de dados Critério 4: Tentativa de acessar rotas exclusivas de aluno DADO QUE o usuário tem role = EGRESSO QUANDO tenta acessar /solicitacoes/nova, /formativas ou /estagios ENTÃO o sistema retorna 403 Forbidden redireciona para /erro/403 exibe mensagem: "Você não tem permissão para acessar este recurso." o botão "Ir ao início" da tela de erro leva para /egresso/inicio Critério 5: Egresso acessa perfil e certificados (rotas reaproveitadas)

DADO QUE o egresso está em /egresso/inicio QUANDO navega para /perfil ENTÃO pode editar e-mail pessoal, telefone e foto (capability user.update\_own\_profile mantida) NÃO vê opções de notificações de solicitação ou formativa (capabilities revogadas) QUANDO

navega para /certificados ENTÃO

vê a lista completa dos certificados emitidos durante o curso pode baixar cada um via botão Download Critério 6: Loading e estado vazio DADO QUE o egresso está em /egresso/inicio QUANDO a API está em processamento ENTÃO exibe DS/Skeleton cobrindo os blocos do dashboard DADO QUE o egresso não tem certificados ENTÃO a seção Certificados exibe DS/EmptyState: "Nenhum certificado emitido durante o curso." Critério 7: Acessibilidade (WCAG 2.1 AA) DADO QUE o egresso usa leitor de tela ENTÃO os  campos  read-only  possuem  atributo  aria-readonly="true"  ou  são  renderizados  como  texto estático a estrutura de headings é: H1 "Olá, {nome}" → H2 por seção (Diploma, Certificados, Colação) os botões de download possuem aria-label descritivo: "Baixar certificado de [atividade]" DIAGRAMAS DE SEQUÊNCIA F2.1-D01 - GET /alumni/me - Dashboard read-only (happy path)

Escopo: Egresso autenticado acessa /egresso/inicio; WebApp carrega dados via GET /alumni/me e renderiza dashboard estritamente read-only com diploma, certificados e dados de colação. Atores: Egresso, WebApp, AlumniController, GetAlumniProfileUC, Postgres Pré-condições:  JWT  válido com alumni.view\_own; usuario.role = EGRESSO; registro de diploma

existe na base.

<!-- image -->

Notas:

Passo  4:  consulta  restrita  ao  alumniId  do  JWT  -  sem  acesso  a  dados  de  outros  egressos  ou listagem de turma (RN-F2.1-06).

Passo 7: resposta HATEOAS - \_links.download no objeto diploma; \_links.reemitir por certificado. O frontend usa useActions(resource) para renderizar botões condicionalmente (RN-F2.1-09).

Dashboard  estritamente  read-only:  nenhum  CTA  de  criação;  todos  os  badges  em  variante success/neutral "Concluído" (RN-F2.1-04/05).

F2.1-D02 - Download Diploma (presigned MinIO)

Escopo: Egresso clica em "Download" do diploma; backend gera URL pré-assinada do MinIO para o PDF oficial sem regenerar o artefato.

Atores: Egresso, WebApp, AlumniController, DiplomaDownloadUC, MinIO

Pré-condições: \_links.download presente na resposta F2.1-D01; arquivo diploma/{uuid}.pdf armazenado no MinIO pela secretaria (US-F5-005).

<!-- image -->

Notas: O PDF é o arquivo oficial armazenado pela secretaria ao registrar o diploma (US-F5-005 tela F5.11);

Passo 4: ttl=900s - URL gerada sob demanda; nenhum novo artefato criado (RN-F2.1-10). não é regenerado nem reassinado.

Diferença de F2.1-D03 (reemissão de certificado): diploma não tem hash/assinatura ED25519 - é documento institucional direto via MinIO presigned.

F2.1-D03 - Reemitir Certificado (presigned MinIO - mesmo hash/assinatura)

Escopo:  Egresso  clica  em  "Reemitir  PDF"  de  um  certificado;  backend  recupera  o  artefato  já existente no MinIO e gera URL pré-assinada - sem criar novo certificado nem nova assinatura. Atores: Egresso, WebApp, CertificateController, ReissueCertificateUC, Postgres, MinIO Pré-condições:  \_links.reemitir  presente  para  o  certificado;  certificate.storage\_key  aponta  para

PDF no MinIO com estado = EMITIDO.

<!-- image -->

Notas:

Passo  4-5:  owner  =  alumniId  previne  IDOR  -  sem  acesso  a  certificados  de  outros  egressos (RN-F2.1-06).

Passos  6-7:  sem  novo  certificado  -  apenas  URL  pré-assinada  para  o  artefato  existente.  Hash SHA-256  e  assinatura  ED25519  são  os  originais  (RN-F2.1-07/08).  Nenhum  INSERT  certificate ocorre.

Passo 9: \_links.verify aponta para /publico/verificar-certificado/{hash} - o certificado reemitido continua verificável publicamente (CA-03). Ver F0/US-F0-007-VERIFICAR-CERTIFICADO.md.

F2.1-D04 - 403: Egresso tenta rota exclusiva de aluno (erro)

Escopo: Egresso (role=EGRESSO, sem request.open) tenta acessar /solicitacoes/nova; RouteGuard do  frontend  bloqueia  antes  mesmo  de  chamar  o  backend; botão "Ir ao início" redireciona para /egresso/inicio.

Atores: Egresso, WebApp

Pré-condições: JWT válido com alumni.view\_own; request.open ausente nas authorities.

<!-- image -->

Notas: Passo 2:  self-call  no  WebApp - RouteGuard verifica authorities[] do JWT em memória; nenhuma chamada  ao  backend  é  feita  (RN-F2.1-02).  A  tela  /egresso/inicio  não  expõe  links  para  rotas exclusivas de aluno (RN-F2.1-04). Se a chamada ao backend ocorrer por bypass direto, @PreAuthorize("hasAuthority('request.open')") retorna  403  Problem  Details  (type:  access\_denied)  -  corpo  em  {title:  "Acesso  negado",  detail: "Capability request.open ausente"}. A tela /erro/403 é coberta em F0/US-F0-005-ERRO.md (F0.5-b).

- 8.4 PROFESSOR

## HU 20. US-F3-001 - Dashboard do Professor

COMO professor autenticado QUERO ver um painel unificado com todas as minhas filas de trabalho (solicitações para deliberar, eventos do dia, formativas CAAF, estágios e TCCs pendentes) PARA ter  visão  imediata  do  que  precisa  da  minha  atenção  e  acessar  rapidamente  cada  função  sem precisar navegar por menus. DESENHO DA(S) TELA(S)

<!-- image -->

[INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Dashboard carregado com blocos HATEOAS

DADO QUE o professor está autenticado com capabilities event.manage e request.deliberate QUANDO acessa /inicio ENTÃO

o BFF retorna blocos para: KpiRow, fila solicitações, meus eventos o bloco "Formativas CAAF" NÃO aparece (professor não tem formative.review) todos os CTAs derivam de \_links na resposta DADO QUE o mesmo professor tem também formative.review ENTÃO o bloco "Formativas CAAF" aparece com a lista de entradas pendentes

Critério 2: KpiCard SLA warning

DADO QUE há 2 solicitações com prazo\_em &lt; now + 24h QUANDO o dashboard carrega ENTÃO o KpiCard de SLA exibe badge warning: "2 solicitações urgentes" ao clicar navega para /solicitacoes?to=me com filtro de urgência aplicado

Critério 3: Evento ativo hoje com destaque

DADO QUE o professor tem um evento em andamento (estado Em\_andamento) QUANDO o dashboard carrega ENTÃO o card do evento exibe badge success "Em andamento" o CTA "Operar evento" leva para /professor/eventos/:id/operacao este CTA aparece somente se \_links.operar existe na resposta

Critério 4: Degradação graciosa

DADO QUE o módulo de solicitações está indisponível QUANDO o BFF tenta agregar os dados ENTÃO o bloco "Fila de solicitações" exibe DS/AlertBanner warning os demais blocos (eventos, formativas) renderizam normalmente DIAGRAMAS DE SEQUÊNCIA

F3.1-D01 - Carregamento inicial do dashboard (happy path - cache MISS)

Escopo: happy path - professor acessa /inicio; cache Redis expirado ou ausente Atores: Professor, WebApp, JwtFilter, DashboardBFF, Redis, Postgres Pré-condições:  professor  autenticado  com  dashboard.view\_self\_professor;  access  token válido; capabilities podem incluir event.manage, request.deliberate e opcionalmente formative.review

<!-- image -->

## Notas:

Passos  8-9:  o  BFF  executa  as  queries  em  paralelo  (coroutines  /  Promise.all);  a  query  de formativasCaaf  só  é  disparada  se  formative.review  estiver  nas  authorities[]  extraídas  do  JWT  - professores sem vínculo à CAAF nunca recebem esse bloco (RN-F3.1-03).

Passo 10:  formativasCaaf=null é retornado no JSON quando a capability está ausente; o frontend interpreta null como ausência do bloco - sem renderização, sem placeholder.

Passo 12: meusEventos[].estado=EM\_ANDAMENTO aciona badge "Em andamento" e \_links.operar disponibiliza o CTA "Operar evento" somente nos cards ativos (CA-03). O useActions oculta o botão se o link estiver ausente (RN-F3.1-05).

Passo 12:  kpis.slaUrgentes  conta  solicitações  com  prazo\_em &lt; now + 24h; o KpiCard exibe badge warning no client-side (CA-02 - DRY, sem HTTP extra).

filaSolicitacoes  é  filtrada  no  BFF  por  canDeliberate=true  para  o  professorId  corrente  -  um

professor não vê solicitações atribuídas a outro (RN-F3.1-04).

F3.1-D02 - Degradação graciosa (módulo de solicitações indisponível)

Escopo: erro parcial de módulo - CA-04, RN-F3.1-06 Atores: Professor, WebApp, JwtFilter, DashboardBFF, SolicitacoesQuery, EventosQuery Pré-condições:  módulo  de  solicitações  lança  timeout  ou  503;  módulos  de  eventos  e  KPIs respondem normalmente

<!-- image -->

Notas: Passo 9: o BFF retorna HTTP 200 mesmo com módulo parcialmente degradado; filaSolicitacoes: null sinaliza ao frontend que o bloco deve exibir DS/AlertBanner warning (RN-F3.1-06). Os blocos de eventos e formativas CAAF (se aplicável) renderizam normalmente. Passo 10: DS/AlertBanner exibe "Não foi possível carregar as solicitações no momento." - CA-04. O

professor conserva acesso a todos os demais blocos.

O BFF usa try/catch isolado por sub-query dentro do agregador; a falha de um módulo não cancela os demais (mesma política de F1.1-D03).

## HU 21. US-F3-002 - Criar, Editar e Operar Eventos Formativos (v4.1)

HU-A - Gerenciar eventos (CRUD) - F3.2a + F3.2b COMO professor com capability event.manage QUERO criar,  editar  e  excluir  eventos  formativos  configurando  modo  de  presença,  janelas  de  validação, carga horária e público PARA oferecer  eventos com controle de presença adequado ao contexto, sem depender da secretaria para configurações. HU-B - Operar evento ao vivo - F3.2c COMO professor organizador no dia do evento QUERO controlar o painel de operação ao vivo - abrir janelas de validação, exibir QR/PIN aos alunos e acompanhar contagens em tempo real PARA conduzir a confirmação de presença de forma segura e verificável.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Criar novo evento DADO QUE o professor está em /professor/eventos \_links.novoEvento existe na resposta QUANDO clica em "Novo evento" ENTÃO navega para o formulário de criação ao preencher: título, curso, inicioEm, fimEm, chCreditadas = 4, attendanceMode SECRET\_SINGLE clicar em "Salvar" ENTÃO o sistema realiza POST /events ao receber 201 Created redireciona para /professor/eventos/:id o evento aparece na lista com estado "AGENDADO" Critério 2: Configuração de janelas de validação (modo DUAL) DADO QUE o professor seleciona attendanceMode = SECRET\_DUAL no formulário QUANDO o formulário atualiza ENTÃO exibe dois blocos de configuração de janela: "Janela de entrada" e "Janela de saída" ambas são obrigatórias antes de salvar ao configurar: entrada 19h00-19h15, saída 21h45-22h00; salvar ENTÃO o evento é criado com duas sub-janelas configuradas Critério 3: Evento concluído é imutável DADO QUE o professor acessa /professor/eventos/:id de um evento com estado CONCLUIDO

=

QUANDO a página carrega ENTÃO todos os campos do formulário estão disabled

um DS/AlertBanner info exibe: "Evento concluído - somente leitura." \_links.excluir NÃO existe na resposta Critério 4: Painel de operação: exibir QR (QR\_SINGLE) DADO QUE o professor está em /professor/eventos/:id/operacao o evento está EM\_ANDAMENTO e attendanceMode = QR\_SINGLE \_links.abrir-janela-entrada existe QUANDO clica em "Abrir janela entrada" ENTÃO o sistema realiza POST /events/:id/attendance/windows/entry o DS/QRDisplay exibe o token QR gerado pelo backend (280×280px) um countdown exibe o tempo restante da janela o QR é renovado automaticamente a cada 5 minutos Critério 5: Painel de operação: exibir PIN (SECRET\_DUAL - fase saída) DADO QUE o professor está no painel de operação de um evento SECRET\_DUAL a fase de entrada já foi concluída \_links.abrir-janela-saida existe QUANDO clica em "Abrir janela saída" ENTÃO o sistema ativa a janela de saída o DS/PINDisplay exibe o PIN de saída em fonte mono 32px alunos que não completaram a entrada ficam marcados como "Inelegíveis" na lista ao vivo Critério 6: Contadores em tempo real DADO QUE o professor está no painel de operação com janela ativa QUANDO os alunos começam a confirmar presença ENTÃO os contadores atualizam a cada 5s:

- Presenças completas: N
- Presenças parciais (duplos): N
- Inelegíveis: N
- a lista ao vivo exibe os últimos nomes confirmados no topo

Critério 7: Encerrar evento e disparar certificados

DADO QUE o professor está no painel de operação \_links.encerrar-evento existe QUANDO clica em "Encerrar evento" e confirma no modal de confirmação ENTÃO o sistema realiza POST /events/:id/close o estado muda para CONCLUIDO para cada aluno com presença completa: CertificateIssuerUseCase emite certificado automaticamente o professor vê DS/AlertBanner success: "Evento encerrado. Certificados sendo gerados."

DIAGRAMAS DE SEQUÊNCIA

- F3.2-D01 - Criar evento (happy path - POST /events)

Escopo: happy path - professor com event.manage submete formulário de criação Atores: Professor, WebApp, JwtFilter, EventController, CreateEventUseCase, Postgres Pré-condições:  professor  autenticado  com  event.manage;  acessou  /professor/eventos  onde \_links.novoEvento está presente Notas: Passo  5:  CreateEventUseCase  valida  antes  do  INSERT:  fimEm  &gt;  inicioEm,  janelas  dentro  do intervalo  do  evento  e  chCreditadas  &gt;  0  (RN-F3.2-07).  Se  inválido → 422  Unprocessable  Entity (Problem Details); nenhum INSERT ocorre. Passo 7: para attendanceMode = QR\_DUAL ou SECRET\_DUAL (CA-02), o payload inclui windows: [{phase:  ENTRY, ...},  {phase:  EXIT,  ...}];  ambas  as  sub-janelas  são  obrigatórias.  O  fluxo  é idêntico; apenas o payload do POST difere.

<!-- image -->

Passo 10: \_links.excluir presente porque estado = AGENDADO (RN-F3.2-06); ausente para eventos EM\_ANDAMENTO ou CONCLUIDO.

F3.2-D02 - Detalhe evento CONCLUIDO (imutável + \_links condicional por estado)

Escopo: professor acessa detalhe de evento encerrado - campos read-only, \_links vazios Atores: Professor, WebApp, JwtFilter, EventController, GetEventUseCase, Postgres Pré-condições: professor autenticado com event.manage; evento existe com estado = CONCLUIDO

<!-- image -->

Notas: Passo 8: o HATEOAS assembler do GetEventUseCase suprime \_links.editar e \_links.excluir quando estado  =  CONCLUIDO  (RN-F3.2-05/06).  Para  estado  =  AGENDADO,  ambos  os  links  estariam presentes;  para  EM\_ANDAMENTO,  apenas  campos  operacionais  (janelas)  seriam  editáveis  via PATCH. RN-F3.2-06  (excluir  via  DELETE):  o  frontend  só  exibe  o  botão  "Excluir"  se  \_links.excluir  estiver presente. A chamada DELETE /events/{id} → 204 No Content segue o mesmo padrão HATEOAS; não gera diagrama separado por ser trivial. Passo 10: DS/AlertBanner info informa o professor sobre a imutabilidade; nenhum botão de edição

é exibido (useActions(\_links) retorna lista vazia).

F3.2-D03 - Painel de operação QR\_SINGLE: abrir janela + QR + polling

Escopo: professor abre janela de entrada em modo QR\_SINGLE, obtém QR e monitora contadores em tempo real

Atores: Professor, WebApp, JwtFilter, EventController, AttendanceWindowUseCase, Postgres Pré-condições:  professor  com event.host; evento EM\_ANDAMENTO; \_links.abrir-janela-entrada presente na resposta anterior Notas: Passo 5: AttendanceWindowUseCase valida internamente que event.estado = EM\_ANDAMENTO e que a janela de entrada não foi aberta anteriormente (RN-F3.2-11); se inválido → 409 Conflict. Passos 10-12: o GET de QR é chamado imediatamente após o POST da janela; todas as requisições HTTP passam pelo JwtFilter (validação JWT em cada chamada) - exibido apenas na primeira para clareza do diagrama. Loop: QR renovado a cada 5 min (TTL do token, RN-F3.2-09); contadores (confirmados, inelegiveis) atualizados a cada 5 s (RN-F3.2-12). Na prática, estas são duas chamadas separadas; o diagrama as mostra consolidadas para brevidade.

<!-- image -->

Para QR\_DUAL: ao concluir a fase de entrada, \_links.abrir-janela-saida aparece; o professor repete o  fluxo  com  POST  .../windows/exit e exibe DS/QRDisplay para a fase de saída - mesmo padrão que D03, endpoint .../qr?phase=exit.

F3.2-D04 - Painel operação SECRET\_DUAL: abrir janela saída + PIN display

Escopo: professor abre a fase de saída em modo SECRET\_DUAL após entrada concluída Atores: Professor, WebApp, JwtFilter, EventController, AttendanceWindowUseCase, Postgres Pré-condições: professor com event.host; evento EM\_ANDAMENTO; fase de entrada já concluída; \_links.abrir-janela-saida presente

<!-- image -->

Notas: Passo 5: o UseCase verifica que a entryWindow está com completed=true antes de abrir a saída; alunos que não completaram a entrada ficam inelegíveis (canExit=false) e aparecem na lista ao vivo (RN-F3.2-10). Passo  8:  exitPin  é  o  PIN  configurado  pelo  professor  no  formulário  do  evento  ou  gerado  pelo sistema; já está armazenado no registro do evento - não é gerado neste momento.

Para SECRET\_SINGLE: apenas POST .../windows/entry é necessário; não há fase de saída. O PIN de entrada é exibido em DS/PINDisplay com o mesmo padrão.

F3.2-D05 - Encerrar evento + outbox (emissão de certificados)

Escopo: professor encerra o evento; backend transiciona para CONCLUIDO e enfileira emissão de certificados via outbox Atores: Professor, WebApp, JwtFilter, EventController, CloseEventUseCase, Postgres Pré-condições: professor com  event.host;  evento  EM\_ANDAMENTO;  \_links.encerrar-evento presente

<!-- image -->

Notas: Passos 6-9: transação atômica - o estado CONCLUIDO e o outbox\_event são gravados na mesma TX. Se o COMMIT falhar, nenhum certificado é emitido (at-least-once garantido pelo outbox). O OutboxDispatcher (a cada 5 s) lê o outbox\_event 'events.closed' e aciona o CertificateIssuerUseCase  para  cada  aluno  com  attendance\_session.completedAt  IS  NOT  NULL. Esse fluxo completo está em → transversal/10.4-certificado-emissao.md.

Encerramento  automático:  o  scheduler  também  pode  acionar  CloseEventUseCase  ao  cruzar scheduledEnd do evento - mesmo fluxo, sem ação do professor.

F3.2-ERRO - 403 FGAC: tentativa de operar janela sem event.host

Escopo: professor (ou usuário) sem event.host tenta acionar janela de presença Atores: Professor, WebApp, JwtFilter, EventController Pré-condições: JWT válido; event.host ausente nas authorities; UI cega (link ausente), mas POST direto ou sessão expirada pode gerar a chamada

<!-- image -->

Notas: Passo 5: em condições normais, o frontend nunca chega a este estado porque useActions(\_links) suprime o botão "Abrir janela entrada" quando \_links.abrir-janela-entrada está ausente (HATEOAS UI cega). O 403 é defesa em profundidade.

Se  o  professor  possui  event.manage  mas  não  event.host  em  seus  próprios  eventos:  a  regra RN-F3.2-08  equipara  event.manage  a  event.host  para  o  organizador  do  evento.  O  conflito  só ocorre quando outro usuário tenta operar um evento de terceiro com apenas event.manage.

## HU 22. US-F3-003 - Deliberar Solicitações (Fila + Deep-link por E-mail)

HU-A - Fila de deliberação (F3.3) COMO professor com capability request.deliberate QUERO ver  todas  as  solicitações  que  aguardam  minha  decisão  em  uma  fila  organizada  com  filtros  e indicadores de SLA PARA priorizar as mais urgentes e garantir que nenhuma solicitação fique sem resposta dentro do prazo. HU-B - Deliberar via sistema ou deep-link de e-mail (F3.4) COMO professor deliberante QUERO deferir,  indeferir,  solicitar  ajustes  ou  encaminhar  uma  solicitação  a  outro  professor,  informando um parecer fundamentado PARA dar andamento formal ao pedido do aluno com registro imutável de auditoria. DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Fila carregada com SLA breach destacado

DADO QUE o professor está em /solicitacoes?to=me

QUANDO a página carrega ENTÃO exibe tabela com: Número, Aluno, Tipo, Prazo (danger se vencido), SLA ordenada por prazo ascendente (mais urgente no topo) por padrão filtrável por tipo, curso e atraso Critério 2: Navegação da fila para deliberação DADO QUE o professor está na fila e clica em uma solicitação QUANDO abre /solicitacoes/:id/deliberar ENTÃO exibe: dados da solicitação, anexos, timeline de eventos painel sticky de decisão com: Textarea "Parecer", botões das ações disponíveis via \_links os botões têm variantes: Deferir (success), Indeferir (danger), Solicitar ajustes (warning), Encaminhar (secondary) Critério 3: Deferir solicitação DADO QUE o professor está na tela de deliberação \_links.deferir existe QUANDO preenche o Textarea com o parecer e clica em "Deferir" ENTÃO o sistema realiza POST /requests/:id/transitions { action: "DEFER", parecer: "..." } ao receber 200 OK: o estado da solicitação muda conforme o workflow a timeline exibe o novo evento "DEFERIDA" com o parecer o aluno recebe notificação push/e-mail o professor é redirecionado para a fila com mensagem de confirmação Critério 4: Deep-link por e-mail (professor não logado) DADO QUE o professor recebeu e-mail com URL /solicitacoes/:id/deliberar?token=JWT não está logado no sistema QUANDO clica no link ENTÃO a tela exibe modo "preview": dados da solicitação visíveis (read-only) um banner info: "Faça login para deliberar esta solicitação." botão "Fazer login" que redireciona para /login mantendo a URL de retorno após login bem-sucedido retorna para /solicitacoes/:id/deliberar?token=JWT o token JWT é validado e as ações ficam disponíveis Critério 5: Deep-link com token já utilizado DADO QUE o professor tenta acessar um deep-link cujo JTI já está na blacklist QUANDO o backend valida o token ENTÃO retorna 401 com detalhe "Token já utilizado" a tela exibe DS/EmptyState: "Este link já foi utilizado. Acesse a fila de solicitações para continuar." botão "Ir para a fila" → /solicitacoes?to=me Critério 6: Encaminhar para outro professor o professor clica em "Encaminhar", seleciona outro professor no select DADO QUE \_links.encaminhar existe na resposta QUANDO clica em "Confirmar encaminhamento" ENTÃO

o sistema realiza POST /requests/:id/transitions { action: "FORWARD", targetUserId: "..." } o sistema gera novo JWT 1-uso para o professor destino enfileira e-mail via Outbox para o novo deliberante a timeline exibe evento "ENCAMINHADA" com nome do destinatário Critério 7: Parecer obrigatório ao indeferir DADO QUE o professor tenta clicar em "Indeferir" sem preencher o Textarea ENTÃO o  campo Textarea exibe borda danger e mensagem: "Informe o parecer para indeferimento (mín. 20 caracteres)." a chamada à API NÃO é realizada DIAGRAMAS DE SEQUÊNCIA F3.3-D01 - Fila de deliberação (GET /requests?canDeliberate=true) Escopo: professor acessa /solicitacoes?to=me e obtém lista filtrada por canDeliberate=true

Atores: Professor, WebApp, JwtFilter, RequestController, Postgres Pré-condições: professor autenticado com request.deliberate Notas: Passo 5: o backend calcula canDeliberate com  base  no  workflow  atual  da  solicitação  + requiresAuthority  da  transição  pendente  ×  authorities  do  professorId.  Solicitações  de  outros deliberantes são invisíveis (RN-F3.3-01). Passo  7:  prazo\_em  &lt;  now  é  computado  pelo  frontend  ao  renderizar  a  célula  de  prazo  (badge danger); sem HTTP extra (RN-F3.3-02). CA-02: clicar em  uma  linha navega para  /solicitacoes/:id/deliberar → /requests/{id}  (Bearer) →

<!-- image -->

WebApp  faz  GET retorna  RequestDto  +  \_links  com  as  ações  disponíveis.  O  \_links determina quais botões são exibidos (RN-F3.4-03) - mesmo padrão HATEOAS de F3.4-D02.

F3.4-D02 - Deferir solicitação (POST /transitions DEFER + TX outbox) Pré-condições: professor autenticado com request.deliberate; \_links.deferir presente na resposta

Escopo: professor delibera DEFER via sistema - transição workflow + TX atômica + outbox Atores: Professor, WebApp, JwtFilter, RequestController, DeliberateRequestUseCase, Postgres de GET /requests/{id}; parecer preenchido (≥1 char)

<!-- image -->

Notas: Passo 5: DeliberateRequestUseCase valida internamente (RN-F3.4-05): (a) authority request.deliberate × Transition.requiresAuthority, (b) guard do workflow satisfeito, (c) JTI não está na blacklist quando  oriundo de deep-link. Qualquer falha → 422  sem  executar  TX  (ver F3.4-ERRO-b). Passos  6-10:  transação  atômica  -  UPDATE estado, INSERT request\_event, INSERT audit\_log e INSERT  outbox\_event  gravados  em  uma  única  TX  (padrão  P4).  Se  o  COMMIT  falhar,  nenhum evento é emitido. Passo 9: o OutboxDispatcher (a cada 5 s) consome solicitacoes.deliberated e dispara push/e-mail ao aluno. Fluxo completo → transversal/10.1-outbox-notificacao.md. RN-F3.4-07  -  Solicitar  ajustes:  mesmo  fluxo  com  action=REQUEST\_ADJUSTMENT;  UPDATE request SET estado=EM\_AJUSTE; outbox solicitacoes.adjustment\_requested → aluno recebe push para corrigir (DRY - não gera diagrama separado). RN-F3.4-08:  request\_event  e  audit\_log  são  imutáveis  após  COMMIT  -  sem UPDATE/DELETE permitidos nessas tabelas.

F3.4-D03 - Deep-link por e-mail: professor não logado (preview → login → validar token)

Escopo: professor acessa deep-link sem sessão ativa - modo preview, login e retorno com ações liberadas Atores: Professor, WebApp, JwtFilter, RequestController, Postgres Pré-condições:  professor  recebeu  e-mail  com URL /solicitacoes/:id/deliberar?token=JWT; JWT

1-uso com audience=request-action, TTL=7d, JTI único; professor não possui sessão ativa

<!-- image -->

Notas: Passo 2:  sem sessão ativa, o WebApp não faz chamada autenticada ao backend - o requestId e o token são lidos da query string para montar o preview local. Passo  5:  o  fluxo  de  login  é  descrito  em  F0/US-F0-001-LOGIN.md  F0.1-a;  a  URL  de  retorno  é

preservada via returnUrl no state do React Router (ou query param).

Passos 10-11: o RequestController valida o deepLinkToken: audience=request-action, sub=professorId (professor correto), exp dentro do prazo, JTI não blacklisted. Token válido não é imediatamente  blacklistado  -  só  após  a  transição  ser  executada  com  sucesso  (para  permitir recarregar a página).

Após o passo 15, o professor preenche parecer e aplica ação → F3.4-D02 (o JTI é blacklistado na TX do DeliberateRequestUseCase).

F3.4-D04 - Encaminhar para outro professor (FORWARD + novo JWT 1-uso + outbox)

Escopo: professor encaminha deliberação a outro professor - gera novo JWT de e-mail e enfileira notificação

Atores: Professor, WebApp, JwtFilter, RequestController, ForwardRequestUseCase, Postgres Pré-condições: professor autenticado com request.deliberate; \_links.encaminhar presente; professor destinatário selecionado Notas: Passo  9:  o  novo  JWT  1-uso  é  gravado  na  tabela  deep\_link\_token  (ou  gerado  on-the-fly  pelo dispatcher  no  passo  10).  A  abordagem  recomendada  é  gerar  o  JTI  e  armazená-lo  na  TX  para garantir atomicidade com o outbox. Passo  10:  o  OutboxDispatcher  consome  solicitacoes.assigned\_to\_user,  renderiza  o  template REQUEST\_NEEDS\_ACTION  com  a  URL  https:/ /app/solicitacoes/{id}/deliberar?token={jwt}  e envia e-mail ao targetUserId. Fluxo completo → transversal/10.1-outbox-notificacao.md. O professor originador não pode mais deliberar após o FORWARD - o deliberatorId mudou e o canDeliberate retorna false para ele.

<!-- image -->

F3.4-ERRO-a - Deep-link com JTI blacklisted (401 token\_already\_used)

Escopo: professor tenta usar deep-link cujo JTI já foi consumido - CA-05, RN-F3.4-05 Atores: Professor, WebApp, JwtFilter, RequestController, Postgres

Pré-condições:  professor  logado  (sessão  válida);  deepLinkToken  na  URL  com  JTI  presente  na blacklist

<!-- image -->

Notas:

Passo 6: JTI  encontrado na blacklist significa que uma transição já foi aplicada com esse token. A resposta é 401 (não 403) pois o token em si é inválido, não a authority do usuário.

O professor pode acessar a solicitação normalmente via /solicitacoes?to=me (F3.3-D01) se ainda houver ações disponíveis para ele.

F3.4-ERRO-b - Authority ou guard inválido na transição (422 guard\_failed)

Escopo: professor tenta aplicar transição sem a authority granular exigida ou com guard workflow não satisfeito - RN-F3.4-05

Atores: Professor, WebApp, JwtFilter, RequestController, Postgres

Pré-condições: professor autenticado; request.deliberate presente, mas  authority granular específica (request.deliberate.tcc) ausente; ou guard do workflow não satisfeito

<!-- image -->

Notas:

Em condições normais, o GET /requests/{id} não retornaria \_links.deferir para um professor sem request.deliberate.tcc  -  UI  cega  via  HATEOAS.  O  422  é  defesa  em  profundidade  contra  UI desatualizada (stale cache) ou chamada direta. O mesmo 422 é retornado se o guard do workflow\_json não for satisfeito (ex.: pré-requisito de estado não atingido). O Problem Details inclui o campo detail com o motivo específico.

## HU 23. US-F3-004 - Revisar Atividades Formativas (CAAF)

COMO professor membro da CAAF (Comissão de Atividades de Formação) QUERO revisar  as  atividades  formativas  submetidas  pelos  alunos  do  meu  curso,  aprovar  ou rejeitar com parecer PARA validar  a  carga  horária  complementar  de  forma  formal,  gerando  automaticamente o certificado quando aprovada.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Fila de formativas para revisão DADO QUE o professor membro da CAAF acessa /formativas?to=me QUANDO a página carrega ENTÃO exibe tabela com: Aluno (nome), Atividade, Horas declaradas, Estado (DS/Badge), Data filtrável por curso e estado professor sem formative.review recebe 403 Critério 2: Aprovar formativa individual DADO QUE o professor clica em uma formativa com estado SUBMETIDA \_links.aprovar existe QUANDO preenche horasValidadas = 4 e clica em "Aprovar" ENTÃO o sistema realiza POST /formative-entries/:id/approve { horasValidadas: 4 } o estado muda para APROVADA CertificateIssuerUseCase emite o certificado para o aluno o aluno recebe notificação: "Sua atividade formativa foi aprovada. Certificado disponível." Critério 3: Aprovar em lote (presença validada)

DADO QUE há 15 formativas do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA submetidas QUANDO o professor seleciona todas e clica em "Revisar em lote" ENTÃO ao confirmar: o sistema aprova todas em uma chamada batch

um modal exibe: "Confirmar aprovação de 15 atividades de evento com presença validada?" cada item ganha seu event\_log individual Critério 4: Rejeitar formativa com parecer DADO QUE o professor clica em uma formativa \_links.rejeitar existe QUANDO preenche o campo parecer com menos de 20 caracteres e tenta rejeitar ENTÃO exibe erro inline: "O parecer deve ter pelo menos 20 caracteres." QUANDO preenche parecer adequado e confirma ENTÃO o estado muda para REJEITADA o aluno recebe push com o parecer DIAGRAMAS DE SEQUÊNCIA F3.5-D01 - Fila de revisão CAAF (GET /formative-entries?canReview=true) Escopo: professor membro da CAAF acessa /formativas?to=me e obtém fila filtrada por escopo de curso Atores: Professor, WebApp, JwtFilter, FormativeController, Postgres Pré-condições: professor autenticado com formative.review; vínculo com CAAF do curso Notas: Passo 4: cursoIds[] extraído do vínculo CAAF do professor no JWT (claim de escopo); ele não vê submissões de alunos de outros cursos (RN-F3.5-02). Passo 7: \_links.batch-approve presente apenas quando há itens do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA  na  página  -  o  frontend  exibe  o  seletor  de  lote somente nesses itens (RN-F3.5-03).

<!-- image -->

F3.5-D02 - Aprovar formativa individual + TX outbox + trigger certificado

Escopo: professor aprova formative\_entry com horasValidadas - TX atômica + outbox que aciona emissão de certificado Atores: Professor, WebApp, JwtFilter, FormativeController, ApproveFormativeUseCase, Postgres Pré-condições: professor com formative.review; \_links.aprovar presente; formative\_entry.estado = SUBMETIDA

<!-- image -->

Notas: Passos 6-10: TX atômica - UPDATE estado, INSERT event\_log e INSERT outbox\_event gravados juntos (RN-F3.5-06). Se o COMMIT falhar, nenhum certificado é emitido. Passo 9: o OutboxDispatcher (a cada 5 s) consome formativas.approved e aciona o CertificateIssuerUseCase para o aluno. O fluxo completo de emissão (Gotenberg PDF + SHA-256 + ED25519 + MinIO) está em → transversal/10.4-certificado-emissao.md. horasValidadas pode diferir das horas declaradas pelo aluno (RN-F3.5-04). O certificado usará o valor validado. Notificação push/e-mail ao aluno após despacho do outbox → transversal/10.1-outbox-notificacao.md.

F3.5-D03 - Aprovar em lote (EVENTO\_INTERNO\_PRESENCA\_VALIDADA)

Escopo: professor aprova N formativas de evento com presença já validada pelo sistema em uma única chamada batch Atores: Professor, WebApp, JwtFilter, FormativeController, BatchApproveUseCase, Postgres Pré-condições: professor com formative.review; \_links.batch-approve presente; itens selecionados são todos do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA

<!-- image -->

Notas: Passo  7:  a  cláusula  WHERE  tipo=EVENTO\_INTERNO\_PRESENCA\_VALIDADA  é  validada  no UseCase antes do UPDATE - itens inválidos (tipo errado ou estado diferente de SUBMETIDA) são excluídos do batch sem cancelar os demais (RN-F3.5-03). Passo 9: cada item gera seu próprio outbox\_event, garantindo que o CertificateIssuerUseCase seja acionado individualmente por aluno → transversal/10.4-certificado-emissao.md. Para  atividades  com  comprovante manual, o lote não está disponível - cada item exige revisão individual (F3.5-D02).

F3.5-D04 - Rejeitar formativa com parecer + TX outbox Escopo:  professor  rejeita  formative\_entry  com  parecer  fundamentado  -  TX  +  outbox  notifica aluno Atores: Professor, WebApp, JwtFilter, FormativeController, RejectFormativeUseCase, Postgres Pré-condições:  professor  com  formative.review;  \_links.rejeitar  presente;  parecer  ≥  20  chars

(validado no frontend antes do POST)

<!-- image -->

Notas: Passo 1:  se  o  parecer  tiver  menos  de  20  caracteres,  o  React  Hook  Form  /  Zod bloqueia o POST antes de ser disparado -  validação client-side, sem  chamada  HTTP  (CA-04  parte 1 → NAO\_APLICAVEL). Passo  9:  o  OutboxDispatcher consome formativas.rejected, renderiza template com o parecer e envia push/e-mail ao aluno para corrigir e  resubmeter  (RN-F3.5-05).  Fluxo  completo → transversal/10.1-outbox-notificacao.md. O  aluno  poderá  corrigir  e  resubmeter  a  atividade → estado  retorna  a  SUBMETIDA  (fluxo  em

F1/US-F1-006-FORMATIVAS.md F1.11-D02).

F3.5-ERRO - 403 FGAC: acesso sem formative.review

Escopo: professor sem formative.review tenta acessar a rota de revisão CAAF - RN-F3.5-01 Atores: Professor, WebApp, JwtFilter, FormativeController

Pré-condições:  professor  autenticado;  formative.review  ausente  nas  authorities  (não  é  membro CAAF)

<!-- image -->

Notas:

Passo  5:  em  condições  normais,  o  menu  "Formativas  CAAF"  não  é  exibido  para  professores  sem formative.review  -  o  BFF  não  retorna  o  bloco  correspondente  no  dashboard  (RN-F3.5-01  · RN-F3.1-03 da US-F3-001). O 403 é defesa em profundidade contra navegação direta pela URL.

## HU 24. US-F3-005 - Emitir Pareceres de Estágio (Orientador / COE)

COMO

professor orientador ou membro do COE QUERO visualizar os estágios sob minha responsabilidade, revisar os documentos enviados pelos alunos e emitir pareceres PARA acompanhar o andamento formal dos estágios e autorizar cada etapa sem processos em papel.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

decisao:  "APROVADO",

CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar estágios para revisão DADO QUE o professor está em /estagios?to=me QUANDO a página carrega ENTÃO exibe tabela com: Aluno, Empresa, Documento pendente se não há estágios: DS/EmptyState "Nenhum estágio aguardando sua revisão." Critério 2: Emitir parecer em documento DADO QUE o professor está em /estagios/:id, tab Documentos o aluno fez upload do Relatório Final e \_links.revisar existe QUANDO preenche o parecer e seleciona "Aprovado" ENTÃO o sistema realiza POST /internships/:id/documents/:docId/review{ parecer: "..."} o documento muda para estado "Aprovado" o aluno recebe notificação Critério 3: Arquivar estágio concluído DADO QUE todos os documentos obrigatórios foram aprovados \_links.arquivar existe QUANDO

o professor clica em "Arquivar estágio" ENTÃO o sistema realiza POST /internships/:id/close o estágio muda para estado CONCLUIDO o aluno recebe notificação de conclusão

DIAGRAMAS DE SEQUÊNCIA

F3.6-D01 - Listar estágios para revisão

Escopo: professor orientador ou membro do COE acessa /estagios?to=me e obtém fila filtrada Atores: Professor, WebApp, JwtFilter, InternshipController, Postgres Pré-condições: professor autenticado com internship.review; vínculo como orientador ou membro do COE

<!-- image -->

Notas:

Passo  5:  o  backend  filtra  por  orientadorId  =  professorId  (estágios  sob  orientação  direta)  ou  por vínculo COE para o curso do aluno (RN-F3.6-01). Professor sem nenhum dos dois vínculos recebe lista vazia ou 403 (ver F3.6-ERRO). Passo 7: documentoPendente é o tipo do documento mais recente com estado=AGUARDANDO\_PARECER - campo calculado no repositório (RN-F3.6-02).

F3.6-D02 - Emitir parecer em documento + TX outbox

Escopo: professor revisa um documento de estágio e emite parecer (APROVADO ou REPROVADO) - TX atômica + notificação outbox Atores: Professor, WebApp, JwtFilter, InternshipController, ReviewDocumentUseCase, Postgres Pré-condições: professor com internship.review; em /estagios/:id (GET carregou \_links.revisar);

parecer preenchido

<!-- image -->

Notas:

Passo 1: antes do POST, o frontend carregou /estagios/:id via GET /internships/{id} - a resposta inclui  os  documentos  e  \_links.revisar  quando  documento.estado  =  AGUARDANDO\_PARECER (RN-F3.6-03). Sem o link, o botão não é exibido (useActions). Passo 9: o OutboxDispatcher consome estagios.document\_reviewed e envia push/e-mail ao aluno com o resultado do parecer (RN-F3.6-06). Fluxo completo → transversal/10.1-outbox-notificacao.md. Passo  12:  \_links.arquivar  aparece  na  resposta  somente  se  todos  os  documentos  obrigatórios  do estágio estiverem com estado=APROVADO (RN-F3.6-05) - HATEOAS condicional.

F3.6-D03 - Arquivar estágio concluído + TX outbox

Escopo: professor arquiva o estágio após aprovação de todos os documentos obrigatórios - TX + notificação outbox ao aluno Atores: Professor, WebApp, JwtFilter, InternshipController, CloseInternshipUseCase, Postgres Pré-condições:  professor  com  internship.review;  \_links.arquivar  presente  (todos  documentos obrigatórios aprovados)

<!-- image -->

Notas: Passo 5: CloseInternshipUseCase valida internamente que todos os documentos obrigatórios estão APROVADO antes de prosseguir. Se algum documento ainda estiver pendente → 422 documents\_pending;  neste  caso  \_links.arquivar  não  deveria  ter  sido  retornado  (defesa  em profundidade). Passo  9:  o  OutboxDispatcher  consome  estagios.closed  e  notifica  o  aluno  de  que  o  estágio  foi concluído formalmente (RN-F3.6-06). Fluxo completo → transversal/10.1-outbox-notificacao.md. Estágios  CONCLUIDO  tornam-se  imutáveis  -  sem  pareceres  adicionais  ou  reabertura  sem capability extra.

F3.6-ERRO - 403 FGAC: acesso sem internship.review

Escopo: professor sem internship.review tenta acessar a rota de revisão de estágios - RN-F3.6-01 Atores: Professor, WebApp, JwtFilter, InternshipController Pré-condições: professor autenticado; internship.review ausente (não é orientador nem membro do COE)

<!-- image -->

Notas: Em  condições  normais,  o  menu  "Estágios"  para  revisão  não  é  exibido  a  professores  sem internship.review - HATEOAS UI cega via dashboard BFF. O 403 é defesa em profundidade contra navegação direta pela URL.

## HU 25. US-F3-006 - Avaliar TCC (Orientador / Banca)

COMO professor orientador ou membro de banca de TCC QUERO visualizar os TCCs sob minha responsabilidade, baixar o arquivo enviado, registrar nota e parecer PARA

formalizar a avaliação e, quando aprovado, desencadear a emissão do certificado de conclusão do aluno.

## DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Listar TCCs para avaliação

DADO QUE o professor acessa /tccs?to=me QUANDO a página carrega ENTÃO exibe tabela com: Aluno, Título, Papel, Situação (DS/Badge) se nenhum TCC pendente: DS/EmptyState "Nenhum TCC aguardando sua avaliação."

Critério 2: Registrar avaliação

DADO QUE o professor acessa /tccs/:id o aluno enviou o arquivo final e \_links.avaliar existe QUANDO preenche nota = 8.5, parecer e clica em "Registrar avaliação" ENTÃO o sistema realiza POST /tccs/:id/review { nota: 8.5, parecer: "...", situacao: "APROVADO" } o estado do TCC muda conforme a nota se aprovado: CertificateIssuerUseCase é disparado o aluno recebe notificação com o resultado

Critério 3: Download do arquivo para avaliação

DADO QUE

o aluno fez upload do TCC QUANDO o professor clica em "Baixar TCC" ENTÃO o sistema gera URL pré-assinada do MinIO (válida 15 min) o download inicia automaticamente

DIAGRAMAS DE SEQUÊNCIA

F3.7-D01 - Listar TCCs para avaliação

Escopo: professor orientador ou membro de banca acessa /tccs?to=me e obtém fila filtrada por canReview=true Atores: Professor, WebApp, JwtFilter, TccController, Postgres Pré-condições: professor autenticado com tcc.supervise; vínculo como orientador, co-orientador

ou membro de banca

<!-- image -->

Notas:

Passo 5: filtra  por  orientadorId  = professorId (orientação direta) ou vínculo de banca para o TCC. Colunas incluem papel (Orientador / Co-orientador / Banca) e situacao (DS/Badge)  - RN-F3.7-01/02.

Passo 7: \_links inclui avaliar por item se \_links.avaliar disponível (aluno enviou arquivo e TCC está em estado aguardando avaliação).

F3.7-D02 - Registrar avaliação + TX outbox + trigger certificado

Escopo:  professor  registra  nota  e  parecer  -  TX  atômica  +  outbox  que  aciona  notificação  e,  se APROVADO, emissão de certificado

Atores: Professor, WebApp, JwtFilter, TccController, ReviewTccUseCase, Postgres Pré-condições: professor com tcc.supervise; \_links.avaliar presente; aluno enviou arquivo final

<!-- image -->

Notas:

Passo  8:  ReviewTccUseCase  calcula  situacao  internamente  com  base  em  nota &gt;= nota\_minima configurada  no  sistema  -  pode  resultar  em  APROVADO,  COM\_CORRECOES  ou  REPROVADO (RN-F3.7-04). O frontend envia apenas nota e parecer; o situacao é determinado pelo backend.

Passo  9:  o  OutboxDispatcher  consome  tcc.reviewed  e:  (a)  envia  push/e-mail  ao  aluno  com  o resultado;  (b)  se  situacao=APROVADO  e  aluno  elegível,  aciona  CertificateIssuerUseCase.  Fluxo completo de emissão → transversal/10.4-certificado-emissao.md. Notificação → transversal/10.1-outbox-notificacao.md.

Passo 7: tcc\_evaluation é imutável após INSERT - cada professor de banca registra a sua avaliação individualmente (RN-F3.7-06). A situacao final do TCC pode depender da consolidação de múltiplas avaliações de banca (regra de negócio a definir no workflow\_json do RequestType).

F3.7-D03 - Download arquivo TCC (presigned MinIO)

Escopo:  professor  baixa  o  arquivo  final  do  TCC  via  URL  pré-assinada  do  MinIO  para  avaliação offline

Atores: Professor, WebApp, JwtFilter, TccController, MinIO

Pré-condições:  professor  com  tcc.supervise;  \_links.download  presente;  aluno  fez  upload  do arquivo final

<!-- image -->

Notas:

Passo 5: a URL pré-assinada expira em 15 min - tempo suficiente para iniciar o download. Se o professor precisar baixar novamente, uma nova chamada a /presign gera nova URL (RN-F3.7-03). O arquivo nunca trafega pelo backend - o download vai diretamente de MinIO para o navegador do professor após o redirecionamento.

F3.7-ERRO - 403 FGAC: acesso sem tcc.supervise

Escopo: professor sem tcc.supervise tenta acessar /tccs?to=me Atores: Professor, WebApp, JwtFilter, TccController Pré-condições: professor autenticado; tcc.supervise ausente (sem vínculo de orientação ou banca)

<!-- image -->

Notas: Em condições normais, o menu "TCCs" para revisão não é exibido a professores sem tcc.supervise - HATEOAS UI cega via dashboard BFF. O 403 é defesa em profundidade.

## HU 26. US-F3-007 - Publicar Comunicado para Turma ou Curso

COMO professor autenticado QUERO escrever e publicar um comunicado em Markdown para minha turma, curso ou todos os alunos, definindo prioridade e data de expiração PARA informar os alunos de forma organizada via hub de comunicação, sem depender de e-mail externo ou grupos de WhatsApp.

DESENHO DA(S) TELA(S)

[INSERIR TELA WEB AQUI]

[INSERIR TELA MOBILE AQUI]

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Formulário de publicação

DADO QUE o professor está em /comunicacao/publicar QUANDO a página carrega ENTÃO exibe: campo título, editor Markdown, preview em tempo real, select audiência, select prioridade, campo expiração o preview usa a mesma tipografia Body do sistema o botão "Publicar" fica habilitado somente quando título e corpo estão preenchidos Critério 2: Publicação bem-sucedida DADO QUE o professor preencheu todos os campos QUANDO clica em "Publicar" ENTÃO o sistema realiza POST /communications { titulo, corpo, audiencia, prioridade, expiraEm } ao receber 201 Created exibe DS/AlertBanner success: "Comunicado publicado com sucesso." o comunicado aparece em /comunicacao para os destinatários o Outbox dispara as entregas por canal conforme a prioridade Critério 3: Preview em tempo real DADO QUE o professor está digitando no editor Markdown QUANDO escreve "## Título" e "negrito" ENTÃO o painel de preview atualiza em tempo real com H2 e texto em negrito o preview é somente leitura (não editável)

Critério 4: Restrição de audiência

DADO QUE o professor tenta selecionar audiência "Todos os alunos da universidade" ENTÃO essa opção NÃO está disponível (sem capability system.broadcast) o select mostra apenas: suas turmas, seus cursos

Critério 5: Responsividade mobile DADO QUE o professor acessa /comunicacao/publicar em dispositivo mobile QUANDO a viewport é &lt; 768px ENTÃO editor e preview ficam em tabs separadas (Editor | Preview) ambas são acessíveis via tabs com aria-selected DIAGRAMAS DE SEQUÊNCIA

F3.8-D01 - Carregar opções de audiência (restrição de scope)

Escopo:  professor abre /comunicacao/publicar e o select de audiência é populado apenas com suas turmas e cursos Atores: Professor, WebApp, JwtFilter, CommunicationController, Postgres Pré-condições: professor autenticado com communication.publish\_class Notas: Passo  5:  O  endpoint  retorna  apenas  as  turmas  e  cursos  vinculados  ao  professor  autenticado  - nunca inclui a opção "todos os alunos da universidade" (requer system.broadcast, fora do escopo deste professor) - CA-04 / RN-F3.8-01. O DS/Select no frontend não precisa filtrar no cliente: a lista já vem pre-filtrada pelo backend. HATEOAS: ausência de system.broadcast na capability lista do token impede a exibição da opção broadcast.

<!-- image -->

F3.8-D02 - Publicar comunicado (POST + TX atômica + outbox fan-out)

Escopo: professor preenche título, corpo Markdown, audiência, prioridade e expiraEm - backend insere Communication + enfileira outbox para fan-out assíncrono Atores:  Professor,  WebApp, JwtFilter,  CommunicationController, PublishCommunicationUseCase, Postgres Pré-condições: professor com communication.publish\_class; audiência selecionada dentro do seu scope Notas: Passo 9: OutboxDispatcher (fora desta TX) consome comunicacao.published e cria communication\_delivery por destinatário conforme audienciaId. O canal de entrega depende da prioridade  (RN-F3.8-02):  CRITICAL → push  imediato  +  ignora  DND;  HIGH → push  +  e-mail; MEDIUM → push se fora do DND; LOW → somente in-app. Fan-out completo → transversal/10.1-outbox-notificacao.md. Passo 8: expiraEm é armazenado em communication.expires\_at (TIMESTAMPTZ). Após esta data, o comunicado permanece no histórico mas perde o status "não lido" nos destinatários - RN-F3.8-03. Passo 13: O redirect para /comunicacao permite ao professor ver o comunicado recém-publicado no hub - RN-F3.8-06.

<!-- image -->

F3.8-ERRO - 403 FGAC + 422 audiência fora de scope

Escopo: dois cenários de erro na publicação de comunicado Atores:  Professor,  WebApp, JwtFilter,  CommunicationController, PublishCommunicationUseCase, Postgres Cenário A - 403 sem communication.publish\_class Cenário B - 422 audiência fora do scope do professor

<!-- image -->

[INSERIR DIAGRAMA DE SEQUÊNCIA AQUI]

Notas: Cenário  A:  em  condições  normais,  o  link  /comunicacao/publicar  não  aparece  no  menu  para professores sem  communication.publish\_class -  HATEOAS  UI  cega.  O  403  é  defesa  em profundidade (URL digitada manualmente). Cenário B: defesa contra adulteração de audienciaId no payload - o backend revalida o vínculo professor ↔ audiência dentro da TX, independente do que o frontend enviou (RN-F3.8-01).

## 8.5 COMISSÕES

## HU 27. US-F4-001 - Pool CAAF: Atribuir e Aprovar Atividades Formativas em Lote

COMO professor membro da CAAF (Comissão de Atividades de Formação) QUERO visualizar o pool coletivo de atividades formativas submetidas pelos alunos do meu curso, atribuir itens  a  colegas  da  comissão  ou  a  mim  mesmo,  e  aprovar  em  lote  atividades  pré-validadas  por presença PARA garantir  que  nenhuma submissão fique sem responsável, distribuir a carga de trabalho entre os membros e acelerar decisões para tipos de atividade que dispensam análise individual.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Pool CAAF carregado com KPIs DADO QUE

o professor com formative.review acessa /comissoes/caaf QUANDO a página carrega ENTÃO o KpiRow exibe: pool total, atribuídas a mim, prazo médio, aprovadas no período

a DataTable exibe: Aluno, Tipo atividade, Horas, Data submissão, Responsável (pool ou nome) itens sem responsável exibem DS/Badge "No pool" itens atribuídos ao usuário exibem DS/Badge "Comigo"

professor sem formative.review recebe HTTP 403 Critério 2: Self-assign DADO QUE o professor está na lista do pool \_links.assign-member existe para um item QUANDO clica em "Atribuir a mim" na linha ENTÃO o sistema realiza POST /commissions/caaf/assign { itemId, assigneeId: "meu-id" } o badge da linha muda para "Comigo" o contador "Atribuídas a mim" no KpiRow incrementa o item aparece na fila individual em /formativas?to=me (F3.5) Critério 3: Atribuir a outro membro via AssignmentBoard DADO QUE o professor está na lista e há outros membros na comissão \_links.assign-member existe QUANDO clica em "Atribuir..." na linha ou no BulkActionBar com 1+ selecionados ENTÃO o DS/AssignmentBoard abre como overlay lateral exibe lista de membros da comissão com nome e carga atual (N itens) o membro com maior carga recebe badge warning QUANDO seleciona um colega e clica em "Confirmar" ENTÃO

o sistema realiza POST /commissions/caaf/assign { itemId, assigneeId: "id-colega" } o overlay fecha e a linha atualiza badge com nome do responsável Outbox enfileira notificação para o colega destinatário

Critério 4: Aprovação em lote (presença validada)

DADO QUE existem 10 formativas do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA no pool QUANDO o professor seleciona todas via checkbox "selecionar todos" a DS/BulkActionBar aparece com "Aprovar selecionados" habilitado clica em "Aprovar selecionados"

ENTÃO um modal de confirmação exibe: "Aprovar 10 atividades de evento com presença validada?" ao confirmar: POST /commissions/caaf/batch-decide { ids: [...], decisao: "APROVADA" } cada item recebe seu event\_log individual CertificateIssuerUseCase é disparado para cada aluno DS/AlertBanner success: "10 atividades aprovadas. Certificados sendo gerados."

Critério 5: Lote com tipo de atividade incompatível

DADO QUE o professor seleciona formativas de tipo misto (presença validada + comprovante manual) QUANDO a DS/BulkActionBar atualiza ENTÃO "Aprovar selecionados" fica desabilitado tooltip: "Selecione apenas atividades de evento com presença validada para aprovação em lote." "Atribuir selecionados" continua habilitado para todos os tipos

Critério 6: Estado Empty (pool vazio)

DADO QUE não há formativas no pool da comissão QUANDO o professor acessa /comissoes/caaf ENTÃO DS/EmptyState exibe: "Nenhuma atividade aguardando revisão no pool."

KpiRow permanece visível com zeros

DIAGRAMAS DE SEQUÊNCIA

F4.1a - Carregar pool CAAF (happy path)

Escopo: happy path - professor com formative.review acessa /comissoes/caaf; API retorna KPIs + pool de formativas (não atribuídas + atribuídas ao próprio usuário). Atores: Professor, WebApp, Tanstack Query,  CAAFController, GetCAAFDashboardUC, Postgres.

Pré-condições: professor autenticado com JWT válido e vinculado a uma CAAF ativa; pelo menos 1 formativa no pool.

<!-- image -->

Notas: Passo  5:  a  query  filtra  estado  NOT  IN  ('APROVADA','REJEITADA')  e  respeita  escopo  de  curso  via commission\_member.curso\_id (RN-F4.1-09). Badges:  assignee  IS  NULL → DS/Badge  "No  pool"  (secondary);  assignee  =  userId → DS/Badge "Comigo" (primary).

\_links  retorna  assign-member  por  item  apenas  quando  formative\_entry.assignee  IS  NULL  ou assignee = userId (FGAC HATEOAS).

F4.1b - Self-assign (happy path)

Escopo: happy path - membro da CAAF atribui um item do pool a si mesmo (\_links.assign-member presente); item migra para fila individual.

Atores: Professor, WebApp, CAAFController, AssignFormativeUC, Postgres.

Pré-condições: item sem responsável no pool; professor com formative.review e escopo de curso compatível.

<!-- image -->

Notas:

Passos  6-9:  transação  atômica garante que a entrada em outbox\_event só existe se o UPDATE confirmar (sem notificação fantasma).

O outbox\_event 'formativas.assigned' com assigneeId = self não dispara push externo ao próprio professor; o dispatcher filtra destinatário ≠ ator da ação.

Após o COMMIT, o item aparece em /formativas?to=me (F3.5) no próximo carregamento da fila individual; o cache TanStack da listagem CAAF é invalidado via queryClient.invalidateQueries(['caaf','dashboard']).

F4.1c - Atribuir a outro membro via AssignmentBoard

Escopo: happy path - professor abre overlay DS/AssignmentBoard, consulta carga dos membros, seleciona colega e confirma atribuição.

Atores: Professor, WebApp, CAAFController, GetCAAFMembersUC, AssignFormativeUC, Postgres.

Pré-condições:  item  no  pool  ou  atribuído  ao  próprio  usuário;  comissão  tem  ≥  2  membros; professor com formative.review.

<!-- image -->

Notas:

Passo  4:  load  é  calculado  como  COUNT(*)  WHERE  assignee\_id  =  member.id  AND  estado  = 'EM\_REVISAO' - reflete apenas itens ativos.

Passo  12:  transação  atômica;  outbox\_event  'formativas.assigned'  com  assigneeId  =  colega.id  -  o dispatcher entrega push/email ao colega ( → ../transversal/10.1-outbox-notificacao.md).

Após  atribuição  ao  colega,  o  item  desaparece  da  listagem  do  professor  atual  (RN-F4.1-02: formativas atribuídas a outro membro não aparecem no pool).

F4.1d - Aprovação em lote - batch-decide (presença validada)

Escopo: happy path - professor seleciona N formativas do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA, confirma modal e aprova em lote; cada item recebe event\_log individual; certificados são disparados via outbox.

Atores: Professor, WebApp, CAAFController, BatchDecideFormativesUC, Postgres.

Pré-condições:  N itens selecionados, todos do tipo EVENTO\_INTERNO\_PRESENCA\_VALIDADA; DS/BulkActionBar habilita "Aprovar selecionados"; professor com formative.review.

<!-- image -->

Notas: Passos 6-10: transação única cobre todos os N updates + event\_logs + um único outbox\_event de lote; o dispatcher fan-out individual por aluno ocorre na fase async ( → ../transversal/10.1-outbox-notificacao.md). O outbox\_event 'formativas.batch\_approved' dispara o CertificateIssuerUseCase via outbox para cada aluno\_id do payload ( → ../transversal/10.4-certificado-emissao.md). formative\_entry\_event\_log  garante  rastreabilidade  individual  mesmo  em  aprovação  coletiva

(RN-F4.1-06 DoD).

F4.1e - ERRO 403 - sem formative.review ou violação de escopo de curso

Escopo: caminhos de erro 403 - (A) professor sem formative.review tenta acessar /comissoes/caaf; (B) professor com formative.review tenta atribuir item de curso fora do escopo da sua comissão.

Atores: Professor, WebApp, CAAFController, Postgres.

Pré-condições:  (A)  professor  autenticado,  JWT  válido,  mas  sem  a  authority  formative.review;  (B) professor com formative.review mas itemId.curso\_id ∉ commission\_member.cursoIds.

<!-- image -->

Notas: Passo 3 (cenário A): o @PreAuthorize("hasAuthority('formative.review')") na CAAFController rejeita antes de qualquer query ao banco; o sidebar nunca renderiza o link sem a authority (UI cega via \_links no BFF dashboard). Passos 6-14 (cenário B): a validação de escopo de curso é feita na camada de use case após a query -  não  confiar  só  no  JWT;  garante  que  mesmo  um  token  válido  não  pode  operar  cross-curso (RN-F4.1-09).

Ambos  retornam  RFC  7807  Problem  Details;  corpo  completo  em  Notas  do  backend:  {type: "access\_denied"|"course\_scope\_violation", title, status:403, detail}.

F4.1f - ERRO 422 - batch-decide com tipos de atividade incompatíveis (guard backend)

Escopo: defesa em profundidade - cliente envia POST /commissions/caaf/batch-decide com ids contendo formativas de tipo misto (não exclusivamente EVENTO\_INTERNO\_PRESENCA\_VALIDADA); backend rejeita com 422 antes de qualquer UPDATE. Atores: Professor, WebApp, CAAFController, BatchDecideFormativesUC, Postgres. Pré-condições: seleção contém ≥ 1 formativa de tipo COMPROVANTE\_MANUAL (ou outro tipo sem aprovação em lote); chamada pode ocorrer por bypass da UI (manipulação direta da API).

[INSERIR DIAGRAMA DE SEQUÊNCIA AQUI] Notas: Passo  6:  a  validação  ocorre  antes  de  qualquer  BEGIN  TX  -  sem  efeito  colateral  no  banco  para payload inválido. No caminho normal da UI, este cenário é prevenido pelo DS/BulkActionBar que desabilita "Aprovar selecionados"  e  exibe  tooltip  quando  a  seleção  é  de  tipos  mistos  (RN-F4.1-07)  -  este  diagrama cobre o guard de backend equivalente. O campo invalidIds no corpo 422 permite ao cliente identificar e destacar os itens incompatíveis na DS/DataTable.

## Execução fila Item: US-F4-001 Status: pendente → feito Arquivo: sequenceDiagrams/F4/US-F4-001-COMISSAO-CAAF.md Próximo: US-F3-002 (ordem 21, pendente) - mas F4-001 foi gerado fora de ordem por solicitação direta; próximo na fila regular é US-F3-002.

## HU 28. US-F4-002 - Pool COE: Atribuir e Acompanhar Estágios em Lote

COMO professor membro do COE (Comitê de Orientação de Estágios) QUERO

visualizar  o  pool  coletivo  de estágios do meu curso/centro aguardando atribuição de orientador, alocar cada estágio a um colega orientador ou a mim mesmo

## PARA

garantir  que  todo  aluno  tenha  um  orientador  responsável  e  que  os  prazos  de  análise  de documentos sejam cumpridos.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Pool COE carregado com KPIs

DADO QUE o professor com internship.review acessa /comissoes/coe QUANDO a página carrega ENTÃO o KpiRow exibe: pool total, atribuídos ao usuário, documentos pendentes, concluídos no período a DataTable exibe: Aluno, Empresa, Tipo estágio, Data início, Documento pendente, Responsável estágios sem orientador exibem DS/Badge "No pool"

documento com SLA vencido exibe célula em status/danger Critério 2: Self-assign de estágio DADO QUE o professor está na lista do pool e \_links.assign-member existe para um estágio QUANDO clica em "Atribuir a mim" ENTÃO o sistema realiza POST /commissions/coe/assign { internshipId, assigneeId: "meu-id" } o badge da linha muda para "Comigo" o contador "Atribuídos a mim" incrementa no KpiRow o estágio passa a aparecer em /estagios?to=me (F3.6) o aluno recebe notificação: "Seu orientador de estágio foi definido: [nome]" Critério 3: Atribuir a outro orientador via AssignmentBoard DADO QUE o COE tem múltiplos membros QUANDO o responsável seleciona um estágio e clica em "Atribuir..." ENTÃO DS/AssignmentBoard abre como overlay lateral exibe lista de membros do COE com: nome, estágios ativos (carga atual)

membro com mais estágios que a média recebe badge warning "Carga alta" QUANDO seleciona um membro e confirma ENTÃO POST /commissions/coe/assign { internshipId, assigneeId } overlay fecha, linha atualiza com nome do orientador Outbox enfileira notificação para o orientador designado Critério 4: BulkActionBar somente com "Atribuir" (sem "Aprovar") DADO QUE o professor seleciona 5 estágios no pool QUANDO a DS/BulkActionBar aparece ENTÃO exibe somente "Atribuir selecionados" habilitado NÃO exibe "Aprovar selecionados" (pareceres de estágio são sempre individuais) QUANDO clica em "Atribuir selecionados" com 5 itens selecionados ENTÃO DS/AssignmentBoard abre com capacidade de atribuir todos de uma vez ao mesmo orientador o sistema realiza POST para cada item (ou endpoint batch) mantendo event\_log individual Critério 5: Estado Empty (pool vazio) DADO QUE não há estágios sem orientador no escopo do COE QUANDO o professor acessa /comissoes/coe

ENTÃO

DS/EmptyState: "Todos os estágios do período já têm orientador atribuído." KpiRow permanece visível mostrando carga de cada orientador

Critério 6: Documento com SLA vencido destacado

DADO QUE um estágio tem documento com prazo de parecer vencido há 3 dias QUANDO o pool carrega ENTÃO a célula "Documento pendente" desse estágio exibe texto em status/danger um ícone de alerta indica o atraso um tooltip informa: "Prazo de parecer vencido há 3 dias"

DIAGRAMAS DE SEQUÊNCIA

F4.2a - Carregar pool COE (happy path)

Escopo: happy path - professor com internship.review acessa /comissoes/coe; API retorna KPIs + lista  de  estágios  (não  atribuídos  +  atribuídos  ao  próprio  usuário);  itens  com  SLA  de  documento vencido incluem document\_due\_date para coloração de perigo no cliente. Atores: Professor, WebApp, COEController, GetCOEDashboardUC, Postgres. Pré-condições:  professor  autenticado,  JWT  válido,  vinculado  a  COE  ativo;  estágios  estado  != 'CONCLUIDO' no escopo do curso.

<!-- image -->

Notas: Passo 5: estado != 'CONCLUIDO' implementa RN-F4.2-10 - estágios encerrados ficam apenas no histórico individual do orientador (/estagios?to=me). Passo 6: document\_due\_date  é  o  prazo  do documento  mais  antigo  ainda  sem  parecer (internship\_document.review\_due\_date WHERE reviewed\_at IS NULL ORDER BY review\_due\_date ASC LIMIT 1); se null, nenhum documento pendente. Passo 12: a comparação document\_due\_date &lt; now() e a renderização status/danger + tooltip com

dias de atraso ocorrem no DS/DataTable client-side (CA-06 - sem chamada backend extra).

F4.2b - Self-assign com notificação ao aluno (happy path) Escopo:  happy  path  - membro do COE atribui um estágio a si mesmo como orientador; aluno

recebe notificação push/email com o nome do orientador designado. Atores: Professor, WebApp, COEController, AssignInternshipUC, Postgres.

Pré-condições: estágio sem orientador no pool; professor com internship.review e escopo de curso compatível; \_links.assign-member presente.

<!-- image -->

Notas: Passos  6-9:  transação  atômica  -  UPDATE  +  outbox\_event  em  commit  único  (sem  notificação fantasma).

O payload {assigneeId=self, alunoId} resulta em dois destinatários no dispatcher: (a) assigneeId = self  é  filtrado  como  ator  da  ação  (sem  auto-notificação);  (b)  alunoId  recebe  push/email  "Seu orientador  de  estágio  foi  definido:  [nome  do  professor]"  (RN-F4.2-05,  CA-02).  Fase  dispatch → ../transversal/10.1-outbox-notificacao.md.

Diferença chave vs CAAF F4.1b: no COE o aluno é notificado no momento da atribuição (orientador

definido), não apenas na conclusão da análise.

F4.2c - Atribuir a outro orientador via AssignmentBoard

Escopo:  happy  path  -  membro  do  COE  abre  DS/AssignmentBoard,  consulta  carga  ativa  dos orientadores, seleciona colega e confirma; orientador E aluno recebem notificação. Atores: Professor, WebApp, COEController, GetCOEMembersUC, AssignInternshipUC, Postgres. Pré-condições: estágio sem orientador ou atribuído ao próprio usuário; COE com ≥ 2 membros;

professor com internship.review.

<!-- image -->

Notas: Passo 4: load = COUNT(*) WHERE assignee\_id = member.id AND estado IN ('EM\_ANDAMENTO', 'AGUARDANDO\_DOC') - reflete apenas estágios ativos em análise. Passo 12 (TX): outbox\_event com payload={assigneeId=orientador, alunoId} → orientador foi definido: [nome]". Fase dispatch → ../transversal/10.1-outbox-notificacao.md.

dispatcher entrega: (a)  push/email  ao  orientador  "Novo  estágio  atribuído  a  você";  (b)  push/email  ao  aluno  "Seu Após atribuição ao colega, o estágio desaparece do pool do professor atual (RN-F4.2-02).

F4.2d - BulkActionBar - atribuição em lote a um orientador Escopo:  happy  path  -  professor  seleciona  N  estágios  no  pool,  abre  DS/AssignmentBoard pelo DS/BulkActionBar  (que  exibe  apenas  "Atribuir  selecionados",  sem  "Aprovar"),  atribui  todos  ao mesmo orientador em um único commit; cada estágio recebe outbox\_event individual.

Atores: Professor, WebApp, COEController, AssignInternshipUC, Postgres.

Pré-condições:  N  estágios  selecionados  no  pool  (todos  do  mesmo  curso/COE);  professor  com internship.review.

<!-- image -->

## Notas:

Passo 1: o DS/BulkActionBar do COE não renderiza "Aprovar selecionados" - diferença intencional em  relação  ao  CAAF  (RN-F4.2-07).  Pareceres  de  estágio  são  juridicamente  sensíveis  e  exigem análise individual por documento.

Passo 13 (TX):  N  outbox\_events individualmente (um por estágio) garantem fan-out correto por aluno → dispatcher  entrega  notificação  ao  orientador  (uma  mensagem  consolidada  ou  N separadas, conforme  template) e N notificações individuais aos  alunos.  Fase  dispatch → ../transversal/10.1-outbox-notificacao.md.

O  endpoint  POST  /commissions/coe/assign  aceita  internshipIds: UUID[] para suporte ao bulk; quando recebe array com 1 elemento, comportamento é idêntico ao F4.2b/F4.2c.

F4.2e - ERRO 403 - sem internship.review ou violação de escopo Escopo: caminhos de erro 403 - (A) professor sem internship.review tenta acessar /comissoes/coe; (B) professor com internship.review tenta atribuir estágio de curso/centro fora do escopo do seu COE.

Atores: Professor, WebApp, COEController, AssignInternshipUC, Postgres.

Pré-condições: (A) JWT  válido, sem  authority internship.review; (B) internship.curso\_id ∉ commission\_member.cursoIds.

<!-- image -->

## Notas:

Passo  3  (cenário  A):  @PreAuthorize("hasAuthority('internship.review')")  rejeita  antes  de  qualquer query; o sidebar BFF não inclui link /comissoes/coe sem a authority (UI cega via \_links).

Passo 11  (cenário  B):  validação  de  escopo  no  use  case  após  query  -  não confiar apenas no JWT; defesa em profundidade contra acesso cross-curso/centro. Padrão  idêntico  ao  F4.1e  (CAAF);  diferenças  apenas  no  nome  da  authority  (internship.review  vs formative.review) e no endpoint.

## Execução fila Item: US-F4-002 Status: pendente → feito Arquivo: sequenceDiagrams/F4/US-F4-002-COMISSAO-COE.md Próximo: US-F3-002 (ordem 21, pendente) - próximo item da fila regular.

8.6 SECRETARIA

## HU 29. US-F5-001 - Dashboard Operacional da Secretaria

COMO secretária acadêmica QUERO ver no meu painel de início os KPIs de solicitações abertas, atrasadas, concluídas hoje e eventos do dia, com alertas visuais de SLA PARA eu possa priorizar meu trabalho sem precisar varrer todas as filas manualmente.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Carregamento e exibição de KPIs DADO QUE a secretária está autenticada com capability dashboard.view\_secretary QUANDO ela acessa /inicio ENTÃO o sistema exibe o estado Skeleton por até 2 s durante o carregamento após a resposta do BFF exibe os KPIs: abertas, atrasadas, concluídas hoje, eventos do dia os valores refletem apenas os cursos vinculados à secretária Critério 2: Destaque de SLA breach DADO QUE existem solicitações com prazo\_em &lt; now() QUANDO o dashboard é exibido ENTÃO cada item na fila priorizada com SLA vencido aparece com texto na cor status/danger um banner de alerta SLA fica visível acima da fila com a contagem de itens em breach Critério 3: Estado Empty DADO QUE não existem solicitações abertas vinculadas aos cursos da secretária QUANDO o dashboard carrega ENTÃO o componente EmptyState é exibido na seção de fila os KPIs de abertas e atrasadas mostram 0

Critério 4: QuickTiles HATEOAS

DADO QUE a resposta do BFF contém \_links para cursos e alunos mas não para importações QUANDO o dashboard renderiza os QuickTiles ENTÃO os tiles de Cursos e Alunos são exibidos o tile de Importações não é exibido

Critério 5: Refresh manual DADO QUE o dashboard está exibindo dados em cache QUANDO a secretária clica no botão de refresh ENTÃO o cache do TanStack Query é invalidado os dados são recarregados com o estado Skeleton

DIAGRAMAS DE SEQUÊNCIA

F5.1-D01 - Carregamento inicial do dashboard (happy path - cache MISS)

Escopo: happy path - secretária acessa /inicio; cache Redis expirado ou ausente Atores: Secretaria, WebApp, JwtFilter, DashboardBFF, Redis, Postgres

Pré-condições: secretária autenticada com  dashboard.view\_secretary; access token válido; cursoIds[] extraídos das capabilities escopeadas

<!-- image -->

Notas:

Passo 6: BFF executa as 4 sub-queries em paralelo (coroutines/awaitAll); filaPriorizada limitada a ≤ 10  registros  ordenados  por  prazo\_em ASC, criado\_em ASC (RN-F5-001-03); todas filtradas por cursoIds[] extraídos do JWT/escopeamento do usuário (RN-F5-001-05).

Passo 6: alertasSla = itens com prazo\_em &lt; now(); filaPriorizada[].sla\_status é calculado pelo BFF (danger se prazo\_em &lt; now(), warning se prazo\_em &lt; now + 24h) - renderização visual client-side via CSS tokens (CA-F5-001-02).

Passo 9: \_links inclui entradas para solicitacoes, alunos, cursos, importacoes - somente os que a secretária possui capability; useActions(\_links) no frontend oculta QuickTiles sem  \_link correspondente (RN-F5-001-07, CA-F5-001-04). Passo  9:  filaPriorizada:  []  retornado  quando  não  há  solicitações → EmptyState  renderizado client-side (CA-F5-001-03, RN-F5-001-06).

F5.1-D02 - Refresh manual (cache invalidado pelo usuário)

Escopo: secretária clica no botão Refresh; TanStack Query invalida cache e força nova chamada Atores: Secretaria, WebApp, JwtFilter, DashboardBFF, Postgres Pré-condições: dashboard já renderizado com dados em cache (staleTime = 60 s); secretária deseja ver dados atualizados antes do TTL natural

<!-- image -->

Notas:

Passo 2: queryClient.invalidateQueries zera apenas o cache client-side (TanStack  Query staleTime=60s); o cache Redis server-side (TTL=60s) pode ainda estar válido. Se Redis HIT ocorrer no  passo  5,  o  BFF  devolve  dados  do  cache  de  servidor  (que  pode  ter  até  60  s  de  defasagem). Comportamento esperado e documentado - para dados 100% em tempo real, o TTL Redis deve ser reduzido.

Passo 2: Skeleton (DS/Skeleton) exibido imediatamente após invalidateQueries, enquanto isLoading=true; oculto ao completar o passo 8 (CA-F5-001-01, CA-F5-001-05).

Redis omitido do diagrama para brevidade; comportamento completo (com HIT/MISS Redis) em F5.1-D01.

F5.1-D03 - Erro 403 FGAC (acesso sem dashboard.view\_secretary)

Escopo: usuário sem capability dashboard.view\_secretary tenta acessar /bff/dashboard/secretary Atores: OutroUsuario, WebApp, JwtFilter, DashboardBFF

Pré-condições: JWT válido; authorities não incluem dashboard.view\_secretary (ex.: aluno, professor sem role de secretaria)

<!-- image -->

## Notas:

Passo 4: @PreAuthorize("hasAuthority('dashboard.view\_secretary')") no controller bloqueia a requisição  antes  de  qualquer  query  ao  Postgres;  resposta  é  RFC  7807  Problem  Details  com type=access\_denied.

Passo 5: a rota /inicio é universal; o frontend detecta o 403 e redireciona para o BFF endpoint correto do perfil do usuário (/bff/dashboard/aluno, /bff/dashboard/professor, etc.) conforme a authority presente no JWT - comportamento DRY via mesma rota (RN-F5-001-01, fluxos\_por\_perfil.md §13). Diagrama relacionado: F5.1-D01 (happy path com capability válida).

## HU  30.  US-F5-002  -  Fila  de  Solicitações,  Nova  Interna,  Deliberar  e Atrasados

COMO secretária acadêmica QUERO gerenciar  a  fila  central  de  solicitações  -  consultando,  filtrando,  abrindo  em  nome  de  alunos, deliberando e monitorando atrasos - PARA eu possa operar o fluxo de trabalho de ponta a ponta sem sair do módulo de solicitações.

DESENHO DA(S) TELA(S)

<!-- image -->

a tabela exibe as solicitações abertas dos cursos vinculados ordenadas por prazo\_em ASC

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Fila com filtros DADO QUE a secretária acessa /solicitacoes QUANDO a página carrega ENTÃO os filtros de estado, tipo, curso e atraso estão disponíveis solicitações com SLA vencido têm a célula SLA em status/danger Critério 2: Nova solicitação interna em nome de aluno DADO QUE a secretária possui capability request.internal\_open QUANDO ela clica em "Nova interna" e busca um aluno por GRR "20231234" ENTÃO o aluno aparece no Combobox QUANDO ela seleciona o aluno e preenche o wizard normalmente confirma a abertura ENTÃO a API recebe POST /requests com o campo onBehalfOf preenchido com o ID do aluno a solicitação aparece na fila com o aluno como titular Critério 3: Deliberar sem deep-link DADO QUE a secretária possui capability request.deliberate uma solicitação na fila tem \_link "deliberate" QUANDO ela clica na linha e acessa /solicitacoes/:id/deliberar ENTÃO a tela de deliberação é exibida (frame F5.4/F3.4) ela pode deferir, indeferir ou solicitar complementação ao confirmar, o estado da solicitação é atualizado na fila Critério 4: Ação em massa (atribuir deliberador)

DADO QUE três solicitações na fila têm \_link bulk\_assign QUANDO a secretária seleciona as três usando os checkboxes escolhe "Atribuir" na DS/BulkActionBar seleciona um professor deliberador ENTÃO

a API recebe PATCH /requests/bulk com os três IDs e o deliberador selecionado a coluna Deliberador das três linhas é atualizada Critério 5: Tela Atrasados DADO QUE existem solicitações com slaBreached=true QUANDO a secretária acessa /secretaria/atrasados ENTÃO a tabela exibe somente essas solicitações o botão "Exportar" está visível QUANDO ela clica em "Exportar" ENTÃO um CSV é baixado com as colunas documentadas Critério 6: Estado Empty da fila DADO QUE não há solicitações abertas para os cursos vinculados QUANDO a secretária acessa /solicitacoes ENTÃO o componente EmptyState é exibido com mensagem "Nenhuma solicitação aberta" DIAGRAMAS DE SEQUÊNCIA

F5.2-D01 - Fila de solicitações (lista paginada com filtros e HATEOAS - happy path)

Escopo:  happy  path  -  secretária  acessa  /solicitacoes;  listagem  filtrada  por  cursos  vinculados; ações por linha via \_links

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições: autenticada com request.view\_curso; cursoIds[] escopeados no JWT; filtros padrão estado=ABERTA&amp;sort=prazo\_em

<!-- image -->

Notas:

Passo  4:  cursoIds[]  é  extraído  das  capabilities  escopeadas  do  JWT  -  a  secretária  nunca  vê solicitações de cursos fora de sua competência (RN-F5-002-01).

Passo 5: \_links é calculado por item no RequestController conforme authorities[] da secretária e estado atual da solicitação (workflow); botões deliberate, assign, encaminhar aparecem somente se o rel correspondente estiver presente (RN-F5-002-04).

Passo 5: sla\_status (danger se prazo\_em &lt; now(), warning se prazo\_em &lt; now + 24h) é calculado no  backend  e  retornado  na  resposta;  renderização  visual  (status/danger,  status/warning)  é client-side - sem HTTP extra (RN-F5-002-03).

Paginação adicional: nova chamada com page=1&amp;size=20; mesmo  fluxo sem  variação de participantes.

F5.3-D02 - Nova interna em nome de aluno (Combobox + POST /requests + TX + outbox)

Escopo: happy path - secretária abre solicitação em nome de aluno; busca por GRR/nome; wizard reutiliza F1.8; onBehalfOf na TX

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições:  autenticada  com  request.internal\_open;  aluno  pertence  a  um  dos  cursos  da secretária; tipos elegíveis já carregados (DRY → F1.8-D02)

<!-- image -->

## Notas:

Passo 4: busca com trigramas PostgreSQL (pg\_trgm) para nome; GRR por igualdade exata; AND curso\_id IN cursoIds[] restringe a alunos da competência da secretária (RN-F5-002-06 - validado também no POST, passo 9).

Passo 7: o wizard percorre os 3 passos de F1.8 (tipos elegíveis → formulário dinâmico → revisar); a diferença é o campo adicional Combobox de aluno (RN-F5-002-05). DRY → F1/US-F1-005-SOLICITACOES.md F1.8-D02, F1.8-D03 para as etapas de tipos e upload de anexo. Passos 10-13: transação atômica - INSERT request + INSERT outbox\_event em único COMMIT; se falhar, nenhum evento é enfileirado (padrão 10.1a). numero\_anual  gerado  atomicamente (ano-NNNN).

Passo 15: dispatch assíncrono notifica o aluno titular (in-app + push + email) via OutboxDispatcher → DRY transversal/10.1-outbox-notificacao.md 10.1b.

F5.4-D03 - Deliberar solicitação sem deep-link (GET detalhe + PATCH + TX + outbox)

Escopo: happy path - secretária acessa /solicitacoes/:id/deliberar diretamente pela fila; delibera sem JWT de email

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições: autenticada com request.deliberate; \_link deliberate presente na resposta da fila (F5.2-D01); RequestType não exige senior\_secretary

<!-- image -->

## Notas:

Passos 2-6: GET /requests/{id} retorna \_links com base nas authorities[] da secretária + estado atual da solicitação. O botão de ação no frontend é renderizado somente se o rel existir - garante que a secretária não vê ações além de sua capability (RN-F5-002-04, RN-F5-002-07).

Passo  7:  fluxo  sem  JWT  deep-link  (diferença  central  em  relação  a  F3.4).  Secretária  acessa diretamente pela fila; sem redirecionamento por email. Mesmo endpoint PATCH /requests/{id}/deliberate (RN-F5-002-07, DRY → F3/US-F3-003-DELIBERAR-SOLICITACOES.md F3.4-D01 quando gerado).

Passos 10-13: TX atômica - UPDATE request, INSERT request\_event, INSERT outbox\_event em COMMIT único (padrão 10.1a). decisao pode ser DEFERIDA, INDEFERIDA ou COMPLEMENTACAO.

Passo 15: OutboxDispatcher notifica o aluno titular (in-app + push + email); DRY transversal/10.1-outbox-notificacao.md 10.1b.

F5.2-D04 - Ação em massa: atribuir deliberador (PATCH /requests/bulk)

Escopo: happy path - secretária seleciona múltiplas solicitações com \_link bulk\_assign e atribui um deliberador em lote

→

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições: autenticada com  request.deliberate; ao menos  uma  solicitação com  \_link bulk\_assign na fila; professor deliberador selecionado

<!-- image -->

Notas:

Passo  1:  DS/BulkActionBar  só  renderiza  checkboxes  em  linhas  cujo  \_link  bulk\_assign  está presente na resposta - linhas em estados incompatíveis não participam da seleção (RN-F5-002-08).

Passo  5:  a  query  filtra  id  IN  (ids)  AND  curso\_id  IN  cursoIds[]  para  prevenir  modificação  de solicitações  fora  do  escopo  da  secretária,  mesmo  com  IDs  forjados;  backend  revalida  capability antes do UPDATE.

Passo  6:  cada  request\_event  registra  tipo=ATRIBUICAO, por=secretariaId, para=deliberadorId - mantém rastreabilidade individual mesmo em ação de lote.

Não  há  outbox\_event  neste  fluxo  (atribuição  não  gera  notificação  ao  aluno  por  padrão);  a notificação  ao  deliberador  pode ser configurada via request\_type.workflow\_json (fora do escopo desta HU).

F5.5-D05 - Atrasados com exportação CSV (slaBreached=true + GET format=csv)

Escopo:  happy  path  -  secretária  acessa  /secretaria/atrasados,  visualiza  fila  SLA  breached  e exporta CSV da página

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições: autenticada com request.view\_curso; existem solicitações com prazo\_em &lt; now()

<!-- image -->

Notas: Passo 6: a resposta da tela Atrasados omite os controles de filtro livre (estado, tipo, curso) pois o filtro slaBreached=true é persistente e fixo (RN-F5-002-09). A DataTable exibe o mesmo conjunto de colunas de F5.2-D01. Passos  8-14:  exportação  síncrona  da  página  atual  -  sem  job  assíncrono;  format=csv  aciona  o marshaller CSV no RequestController (RN-F5-002-10). Campos do CSV: Número, Tipo, Aluno, GRR, Curso, Estado, Deliberador, Data Abertura, Prazo, Dias de Atraso. Passo  13:  Content-Disposition:  attachment;  filename=atrasados-{date}.csv;  encoding UTF-8 BOM para compatibilidade com Excel.

Escopo:  erro  de  escopo  -  secretária  tenta  abrir  solicitação  em  nome  de  aluno  de  curso  não vinculado Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres Pré-condições:  secretária  possui  request.internal\_open; onBehalfOf aponta para aluno de curso

F5.3-ERRO - 403 nova interna: aluno fora do escopo de cursos da secretária fora de cursoIds[]

<!-- image -->

Notas: Passo 4: o RequestController verifica alunoId.curso\_id IN cursoIds[] antes de qualquer INSERT - mesma  validação  feita  no  Combobox  (passo  4  de  F5.3-D02),  mas  repetida  no  backend  por defense-in-depth (RN-F5-002-06). Passo 6: RFC 7807 Problem Details type=forbidden\_scope; corpo completo em Notas - não inline na seta. Passo  7:  DS/AlertBanner  exibe  "O  aluno  selecionado  não  pertence  aos  cursos  vinculados  à  sua

conta." - permite que a secretária corrija o aluno no Combobox sem sair do wizard.

F5.4-ERRO - 403 deliberar: RequestType exige senior\_secretary ausente

Escopo:  erro  de  autoridade  insuficiente  -  secretária  tenta  deliberar  RequestType  que  requer

capability senior\_secretary Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres Pré-condições: secretária possui request.deliberate mas não senior\_secretary; RequestType tem

requires\_capability: senior\_secretary

<!-- image -->

## Notas:

Passo 4: o RequestController consulta request\_type.requires\_capability para o tipo da solicitação; se  a  capability  exigida  não  estiver  nas  authorities[]  do  JWT,  a  deliberação  é  bloqueada  antes de qualquer mutação (RN-F5-002-07).

Observação HATEOAS: idealmente o \_link deliberate não deveria estar presente para este usuário neste RequestType (o BFF deveria omiti-lo); o diagrama documenta a defesa backend para o caso de \_link presente indevidamente ou acesso direto à URL.

Passo 7: DS/AlertBanner exibe "Você não possui permissão para deliberar este tipo de solicitação. Contate um secretário sênior." - orienta a ação corretiva.

## HU 31. US-F5-003 - Gestão de Alunos

## COMO

secretária acadêmica

QUERO

buscar,  cadastrar, editar e administrar contas de alunos (incluindo reset de senha e matrícula em disciplinas)

PARA

eu possa manter o cadastro atualizado sem precisar de acesso direto ao banco de dados.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Busca de alunos

DADO QUE a secretária acessa /secretaria/alunos QUANDO ela digita "João" na barra de busca

ENTÃO

a tabela exibe os alunos cujo nome contém "João" (trigrama) cada linha mostra: Nome, GRR, Curso, Período, Situação Critério 2: Cadastrar novo aluno DADO QUE a secretária clica em "Novo aluno" QUANDO o Drawer abre e ela preenche todos os campos obrigatórios clica em "Salvar" ENTÃO a API recebe POST /students com os dados o aluno aparece na tabela um e-mail de boas-vindas com senha temporária é enviado via Outbox Critério 3: Editar aluno DADO QUE a secretária clica em "Editar" na linha de um aluno do seu curso QUANDO o Drawer abre com os dados pré-preenchidos ela altera o período de ingresso salva ENTÃO a API recebe PATCH /students/:id a linha na tabela reflete a alteração sem recarregar a página Critério 4: Reset de senha DADO QUE a secretária clica em "Reset senha" para um aluno QUANDO

um dialog de confirmação aparece e ela confirma ENTÃO a API envia POST /students/:id/reset-password o aluno recebe e-mail com nova senha temporária audit\_log registra a ação com o ID da secretária e timestamp mustChangePassword é marcado como true para o aluno

Critério 5: Conflito de GRR

DADO QUE já existe um aluno com GRR "20231234" QUANDO a secretária tenta cadastrar outro aluno com o mesmo GRR ENTÃO a API retorna HTTP 409 o Drawer exibe uma mensagem de erro inline no campo GRR

Critério 6: Aluno de outro curso (HATEOAS)

DADO QUE a secretária busca um aluno de um curso que ela não gerencia QUANDO o aluno aparece nos resultados ENTÃO as ações "Editar", "Reset senha" e "Matricular" não são exibidas para essa linha uma tooltip indica "Sem permissão para este curso"

DIAGRAMAS DE SEQUÊNCIA

F5.6-D01 - Busca de alunos (lista paginada + HATEOAS por linha - happy path)

Escopo:  happy  path  -  secretária  pesquisa  alunos  por  nome/GRR/e-mail;  ações  por  linha controladas por \_links Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres Pré-condições:  autenticada  com  user.manage\_students;  cursoIds[]  no  JWT;  busca  dispara  com debounce 300 ms Notas: Passo  4:  busca  com  pg\_trgm  para  nome  (trigramas,  case-insensitive);  grr  por  igualdade  exata; email por ILIKE prefix%. Os três campos combinados com OR (RN-F5-003-02). Busca não filtra por cursoIds[] - alunos de qualquer curso aparecem nos resultados. Passo  5:  \_links  calculados  por  item:  edit,  reset-password,  matricula  presentes  somente  se aluno.curso\_id  IN  cursoIds[];  alunos  de  outros  cursos  retornam  sem  \_links  de  ação  -  botões ocultos no frontend (RN-F5-003-08, CA-F5-003-06). Paginação adicional: nova chamada com page=1; mesmo fluxo sem variação de participantes.

<!-- image -->

F5.6-D02 - Cadastrar novo aluno (POST /students + audit\_log + outbox email boas-vindas) Escopo: happy path - secretária preenche Drawer e cria novo aluno; TX inclui senha temporária Argon2id + audit\_log + outbox Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres Pré-condições: autenticada com user.manage\_students; GRR e CPF ainda não existem no sistema Notas: Passo 4: StudentController gera a senha temporária em memória (não persistida em texto claro) e calcula  o  hash  Argon2id  antes  de  abrir  a  TX  -  a  senha  temporária  só  trafega  no  payload  do outbox\_event  (criptografado  em  trânsito  via  TLS;  template  de  e-mail  é  o  único  ponto  de exposição). Passos  5-9:  TX  atômica  -  INSERT  usuario  +  INSERT  audit\_log  +  INSERT  outbox\_event  em COMMIT único; se falhar, nenhum evento é enfileirado e nenhum e-mail é enviado (padrão 10.1a). Passo  11:  dispatch  assíncrono  via  OutboxDispatcher  envia  e-mail  de  boas-vindas  com  senha temporária;  DRY → transversal/10.1-outbox-notificacao.md 10.1b.  mustChangePassword=true força troca no próximo login (DRY → F1/US-F1-002-PRIMEIRO-ACESSO.md).

<!-- image -->

F5.6-D03 - Editar aluno (PATCH /students/:id + audit\_log)

Escopo:  happy  path  -  secretária  abre  Drawer  pré-preenchido,  altera  campo  e  salva;  resposta atualiza linha sem reload

Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres

Pré-condições:  autenticada  com  user.manage\_students;  \_link  edit  presente  na  linha  (aluno do curso gerenciado)

<!-- image -->

## Notas:

Passo 5: AND curso\_id IN cursoIds[] previne que a secretária edite alunos de cursos fora de sua competência mesmo com PATCH direto à URL (RN-F5-003-08); retorna 404 se id não pertencer ao escopo (preferível a 403 para não vazar existência de registros).

Passo  6:  audit\_log  persiste  diff  -  objeto  com  {campo,  de,  para}  para  cada  campo  alterado  -  e operadorId (RN-F5-003-07). Campos não alterados não integram o diff.

Passo  9:  queryClient.invalidateQueries([students])  (RN-F5-003-09)  aciona  nova  chamada  GET /students pelo TanStack Query hook, atualizando a tabela sem window.location.reload().

F5.6-D04 - Reset de senha administrativo (POST reset-password + Argon2id + mustChangePassword + outbox + audit\_log)

Escopo:  happy  path  -  secretária  reseta  senha  de  aluno;  nova  senha  temporária  Argon2id, mustChangePassword e e-mail via outbox

Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres

Pré-condições: autenticada com user.manage\_students; \_link reset-password presente (aluno do curso gerenciado)

<!-- image -->

Notas: Passo 4: geração de senha temporária em memória antes da TX; nunca persistida em texto claro. Hash Argon2id calculado no mesmo passo (RN-F5-003-05). Passo  6:  mustChangePassword=true  força  redirecionamento  ao  fluxo  de  primeiro  acesso  no próximo login do aluno (DRY → F1/US-F1-002-PRIMEIRO-ACESSO.md F1.2-D01 guard mustChangePassword). Passo  7:  audit\_log  registra  tipo=user.password\_reset,  operadorId,  targetUserId,  timestamp;  a senha  temporária  não  é  registrada  no  audit\_log  (RN-F5-003-07).  A  secretária  em  nenhum momento  visualiza  a  senha  -  ela  é  entregue  diretamente  ao  aluno  por  e-mail  (segue  padrão fluxos\_por\_perfil.md §8 F7.6). Passo 11: dispatch async → transversal/10.1-outbox-notificacao.md 10.1b.

Template PASSWORD\_RESET\_BY\_ADMIN (diferente do self-service PASSWORD\_RESET de F0.2-a).

F5.6-D05 - Matrícula em disciplina (POST /matricula + validação de vagas + audit\_log)

Escopo: happy path - secretária vincula aluno a disciplinas do período vigente; validação de vagas antes do INSERT

Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres

Pré-condições:  autenticada  com  user.manage\_students;  \_link  matricula  presente;  disciplinas existem no catálogo com vagas disponíveis

<!-- image -->

## Notas:

Passo 4: SELECT ... FOR UPDATE trava as linhas de disciplina antes da TX para prevenir condição de corrida em matrículas concorrentes (RN-F5-003-06). Se qualquer disciplina retornar vagas\_disponiveis = 0, o fluxo desvia para F5.6-ERRO-02 antes de abrir a TX.

Passo  7:  UPDATE  vagas\_disponiveis  -=  1  e  INSERT  matricula  executados  em  lote  por  disciplina dentro da mesma TX; rollback automático se qualquer INSERT falhar.

Sem outbox\_event neste fluxo - notificação de matrícula ao aluno é opcional e configúrável em request\_type.workflow\_json; fora do escopo desta HU.

F5.6-ERRO-01 - 409 conflito de GRR ou CPF

Escopo: erro de unicidade - secretária tenta cadastrar aluno com GRR ou CPF já existente

Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres Pré-condições: POST /students disparado; GRR ou CPF já existe em usuario

<!-- image -->

## Notas:

Passo 5: o erro Postgres  23505  (unique\_violation)  é  capturado  pelo  ExceptionHandler;  o StudentController extrai o nome da constraint para identificar o campo conflitante (grr ou cpf) e compõe o Problem Details correspondente (RN-F5-003-04).

Passo 6: RFC 7807 Problem Details com type=conflict, field=grr, value=20231234; corpo completo em Notas - não inline na seta.

Passo  7:  o  Drawer  permanece  aberto com o campo GRR destacado em status/danger; nenhum dado é perdido - secretária pode corrigir o valor sem reabrir o Drawer.

F5.6-ERRO-02 - 422 matrícula sem vagas disponíveis

Escopo: erro de validação - secretária tenta matricular aluno em disciplina sem vagas Atores: Secretaria, WebApp, JwtFilter, StudentController, Postgres

Pré-condições: POST /students/:id/matricula disparado; ao menos uma disciplina com vagas\_disponiveis = 0

<!-- image -->

Notas:

Passo 4: verificação feita antes de abrir a TX - evita rollback desnecessário e LOCK em disciplinas válidas da mesma requisição (RN-F5-003-06).

Passo  6:  RFC  7807  Problem  Details  type=no\_vacancies;  body  inclui  disciplinaId  e  nome  da disciplina para facilitar correção pelo operador.

O  Drawer  permanece  aberto;  secretária  pode  desmarcar  a  disciplina  sem  vagas  e  confirmar somente as disponíveis.

F5.6-ERRO-03 - 403 FGAC: acesso sem user.manage\_students

Escopo: erro de autorização - usuário sem capability user.manage\_students tenta acessar a tela de gestão de alunos

Atores: OutroUsuario, WebApp, JwtFilter, StudentController

Pré-condições: JWT válido; authorities não incluem user.manage\_students

<!-- image -->

## Notas:

Passo  4:  @PreAuthorize("hasAuthority('user.manage\_students')")  no  StudentController  bloqueia antes de qualquer query ao Postgres; resposta RFC 7807 type=access\_denied (RN-F5-003-01). Passo  5:  o  frontend  detecta  o  403  e  redireciona  para  o  BFF  endpoint  do  perfil  do  usuário

(/bff/dashboard/aluno, /bff/dashboard/professor, etc.) via lógica análoga ao F5.1-D03.

## HU 32. US-F5-004 - Dados Acadêmicos: Cursos, Disciplinas e Calendários

COMO

secretária acadêmica QUERO manter o cadastro de cursos, disciplinas e calendário acadêmico PARA o  sistema  disponha  de  dados  de  referência  corretos  para  regras  de  elegibilidade,  carga  horária,

prazos e exibição nos demais módulos.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: CRUD de Curso DADO QUE a secretária acessa /secretaria/cursos QUANDO ela clica em "Novo" preenche Nome, Sigla, Coordenador, Horas formativas e Secretários salva ENTÃO a API recebe POST /secretaria/cursos o novo curso aparece na tabela o vínculo de secretários é salvo, refletindo em request.view\_curso Critério 2: Sigla de curso duplicada DADO QUE já existe um curso com sigla "TADS"

QUANDO a secretária tenta criar outro curso com sigla "TADS" ENTÃO a API retorna HTTP 409 o formulário exibe erro inline no campo Sigla

Critério 3: CRUD de Disciplina

DADO QUE a secretária acessa /secretaria/disciplinas QUANDO ela cria uma disciplina com Nome "Banco de Dados", Código "BDD01", Curso "TADS", Carga 60h ENTÃO a API recebe POST /secretaria/disciplinas a disciplina aparece na tabela com situação Ativa

Critério 4: Desativar disciplina

DADO QUE existe uma disciplina ativa QUANDO a secretária a desativa via toggle ENTÃO o campo Ativa é atualizado para false via PATCH alunos já matriculados permanecem vinculados a disciplina aparece com badge "Inativa" na tabela Critério 5: Criar período letivo DADO QUE a secretária acessa /secretaria/calendarios na aba Períodos QUANDO ela cria um período "2026/2" com início 01/08/2026 e fim 30/11/2026 ENTÃO a API recebe POST /calendars/periods o período aparece na visualização mensal Critério 6: Sobreposição de período letivo DADO QUE já existe um período de 01/08/2026 a 30/11/2026 para o curso TADS QUANDO a secretária tenta criar período de 01/10/2026 a 28/02/2026 para o mesmo curso ENTÃO a API retorna HTTP 422 com mensagem "Período sobrepõe 2026/2"

Critério 7: Criar evento de calendário

DADO QUE a secretária acessa a aba Eventos QUANDO ela cria um evento tipo COLACAO em 15/11/2026 com título "Colação de Grau TADS" ENTÃO o evento aparece no dia 15/11 com a cor semântica roxa

DIAGRAMAS DE SEQUÊNCIA

F5.7-D01 - Criar curso com vínculo de secretários (POST /secretaria/cursos + audit\_log)

Escopo: happy path - secretária cria novo curso e vincula secretários responsáveis; vínculo reflete em request.view\_curso

Atores: Secretaria, WebApp, JwtFilter, CourseController, Postgres

Pré-condições:  autenticada  com  course.manage;  coordenadorId  e  secretariosIds[]  são  usuários existentes

<!-- image -->

Notas:

Passo  6:  INSERT  curso\_secretario  materializa  o  vínculo  secretária ↔ curso;  esse  vínculo  é consultado pelo JwtFilter na montagem de cursoIds[] no JWT para a capability request.view\_curso (RN-F5-004-03).  Alterações  no vínculo exigem re-emissão do token ou invalidação de cache de autorização.

Passos 4-8: TX única - se o INSERT dos secretários falhar (ex.: usuário inválido), o curso também não é criado. Consistência garantida sem compensação manual.

Desativar curso: PATCH /secretaria/cursos/{id} {ativo: false} - DRY → F5.6-D03 (mesmo padrão PATCH + audit\_log; RN-F5-004-04: histórico de alunos e solicitações preservado).

F5.8-D02 - Criar disciplina (POST /secretaria/disciplinas + audit\_log)

Escopo: happy path - secretária cria nova disciplina vinculada a um curso

Atores: Secretaria, WebApp, JwtFilter, SubjectController, Postgres

Pré-condições: autenticada com subject.manage; cursoId existe no catálogo

<!-- image -->

Notas:

Passo 5: codigo é UNIQUE por cursoId (não globalmente); a API retorna 409 se houver duplicidade no  mesmo  curso  (análogo  a F5.7-ERRO-01; não desenhado separadamente - mesmo padrão de constraint unique\_violation).

Editar  disciplina  (nome,  ch,  período):  DRY → F5.6-D03  (PATCH  /secretaria/disciplinas/{id}  + audit\_log + invalidateQueries[disciplinas]).

F5.8-D03 - Desativar disciplina (PATCH ativa=false + audit\_log)

Escopo: happy path - secretária desativa disciplina via toggle; alunos já matriculados permanecem vinculados

Atores: Secretaria, WebApp, JwtFilter, SubjectController, Postgres

Pré-condições: autenticada com subject.manage; disciplina existe e está ativa

<!-- image -->

Notas:

Passo 5: UPDATE disciplina SET ativa=false não afeta a tabela matricula - alunos já matriculados mantêm o vínculo; desmatrícula manual exige ação separada (RN-F5-004-07).

Passo 8: \_links retornados: se ativa=false, o link deactivate é substituído por activate; o frontend exibe toggle "Reativar" via useActions(\_links).

Padrão idêntico ao PATCH /secretaria/cursos/{id} {ativo: false} para desativar curso (DRY).

F5.8-D04-CSV - Exportar CSV de disciplinas (GET ?format=csv)

Escopo: happy path - secretária exporta disciplinas do curso corrente em formato CSV Atores: Secretaria, WebApp, JwtFilter, SubjectController, Postgres Pré-condições: autenticada com subject.manage; ao menos uma disciplina cadastrada

<!-- image -->

Notas: Passo  4:  sem  LIMIT  -  a  exportação  inclui  todas  as  disciplinas  do  curso;  pode  ser  filtrada  pelos mesmos parâmetros da tela (ex.: ativa=true se filtro estiver ativo).

Passo 6: Content-Disposition: attachment; filename=disciplinas-{sigla}-{date}.csv; UTF-8 BOM para compatibilidade com Excel (RN-F5-004-08). Mesmo padrão de F5.5-D05 passos 8-14 (DRY).

F5.9-D05 - Criar período letivo (POST /calendars/periods + validação sobreposição + audit\_log) Escopo:  happy  path  -  secretária  cria  período  letivo  para  um  curso;  validação  de  sobreposição antes da TX Atores: Secretaria, WebApp, JwtFilter, CalendarController, Postgres Pré-condições: autenticada com calendar.manage; nenhum período com datas sobrepostas para o

mesmo curso.

<!-- image -->

Notas: Passo  4:  verificação  de  sobreposição  feita  antes  da  TX  com  query  de  intervalo  [inicio\_novo, fim\_novo] vs [inicio\_existente, fim\_existente]; sem FOR UPDATE (leitura não-destrutiva); se linha retornar → desvia para F5.9-ERRO-02 (RN-F5-004-12). Passo  7:  ao  criar  um  período  com  ativo=true,  outros  períodos  ativos  do  mesmo  curso  não  são desativados automaticamente  -  a secretária  pode  ter  períodos  sobrepostos  entre  cursos diferentes. Sobreposição bloqueada somente dentro do mesmo cursoId.

RN-F5-004-13: o BFF do dashboard (F5.1-D01) emite alerta de alertasSla[] quando não há período ativo configurado; a criação bem-sucedida de um período remove esse alerta no próximo refresh.

Escopo:  happy  path  -  secretária  cria  evento  de  data  especial  (colação,  feriado,  prazo)  no

F5.9-D06 - Criar evento de calendário (POST /calendars/events + audit\_log) calendário do curso Atores: Secretaria, WebApp, JwtFilter, CalendarController, Postgres

Pré-condições: autenticada com calendar.manage; período letivo vigente existe para o curso Notas:

<!-- image -->

Passo  5:  tipo  é  um  enum  {FERIADO,  COLACAO,  PRAZO,  INSTITUCIONAL};  a  cor  semântica  é calculada  no  backend  e  retornada  no  campo cor (purple, gray, orange, blue), permitindo que o frontend aplique diretamente o token CSS sem lógica de mapeamento própria (RN-F5-004-11).

Múltiplos eventos podem coexistir na mesma data (ex.: PRAZO + INSTITUCIONAL); sem validação de unicidade por data.

PATCH e DELETE de eventos: DRY → F5.6-D03 (PATCH) / padrão simples DELETE /calendars/events/{id} + audit\_log, não desenhado separadamente.

## F5.7-ERRO-01 - 409 sigla de curso duplicada

Escopo: erro de unicidade - secretária tenta criar curso com sigla já existente Atores: Secretaria, WebApp, JwtFilter, CourseController, Postgres Pré-condições: sigla informada já existe em curso

<!-- image -->

Notas:

Passo  5:  23505  unique\_violation  capturado  pelo  ExceptionHandler; extrai o nome da constraint (curso\_sigla\_key)  para  identificar  o  campo  conflitante  (CA-F5-004-02,  RN-F5-004-02).  Padrão idêntico a F5.6-ERRO-01 (GRR/CPF).

O  formulário  permanece  aberto  com  o  campo  Sigla  destacado  em  status/danger;  secretária corrige sem perder os demais campos preenchidos.

F5.9-ERRO-02 - 422 sobreposição de período letivo

Escopo: erro de validação - secretária cria período com datas sobrepostas a período existente do mesmo curso

Atores: Secretaria, WebApp, JwtFilter, CalendarController, Postgres

Pré-condições: período 2026/2 (01/08-30/11/2026) já existe para curso TADS

<!-- image -->

Notas:

Passo 4: query de intervalo usa a condição clássica de sobreposição A.inicio &lt;= B.fim AND A.fim &gt;= B.inicio; feita antes de abrir TX para evitar lock desnecessário (RN-F5-004-12).

Passo 6: RFC 7807 Problem Details type=period\_overlap; body inclui conflictsWithId e conflictsWithNome para que o frontend mostre o nome do período conflitante na mensagem de erro.

O modal permanece aberto; secretária pode ajustar as datas sem perder o formulário.

## HU 33. US-F5-005 - Egressos e Colação de Grau

COMO

secretária acadêmica

## QUERO

gerenciar o cadastro de egressos e registrar o processo de colação de grau e entrega de diploma PARA

a  transição  do  aluno  para  o  perfil  EGRESSO  seja  documentada,  o  diploma  seja  entregue formalmente e o egresso receba acesso ao portal.

## DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Listar egressos DADO QUE a secretária acessa /secretaria/egressos QUANDO a página carrega ENTÃO a tabela exibe os egressos com Nome, Curso, Ano de Colação e Situação do diploma filtros de curso, ano e situação estão disponíveis Critério 2: Iniciar processo de colação DADO QUE a secretária acessa /secretaria/diplomas QUANDO ela escolhe o curso "TADS" e o período "2026/2" ENTÃO o Passo 1 exibe a lista de elegíveis com checkboxes alunos inelegíveis aparecem desabilitados com tooltip explicativo Critério 3: Confirmar colação DADO QUE a secretária selecionou 5 alunos elegíveis no Passo 1 QUANDO ela avança para o Passo 2 e preenche data da cerimônia, livro e folha clica em "Confirmar colação" ENTÃO a API recebe POST /graduations com os dados os 5 alunos têm role atualizado para EGRESSO cada um recebe e-mail de boas-vindas via Outbox um graduation\_record é criado para cada aluno Critério 4: Aluno inelegível DADO QUE um aluno tem apenas 60 horas formativas e o mínimo é 120 QUANDO a secretária abre o wizard de colação para o curso do aluno ENTÃO o aluno aparece na lista mas com checkbox desabilitado o tooltip exibe "Horas formativas insuficientes: 60/120 h"

Critério 5: Confirmar entrega de diploma

DADO QUE um egresso tem graduation\_record com diploma PENDENTE QUANDO a secretária clica em "Confirmar entrega" e seleciona "Retirada presencial" com a data atual ENTÃO a API recebe PATCH /graduations/:id/confirm-delivery a situação do diploma muda para ENTREGUE na lista de egressos

DIAGRAMAS DE SEQUÊNCIA

F5.10-D01-EGRESSOS - Listar egressos (GET /secretaria/egressos + filtros - happy path)

Escopo:  happy  path  -  secretária  acessa  lista  de  egressos  filtrada  por  curso,  ano  de  colação  e situação do diploma Atores: Secretaria, WebApp, JwtFilter, EgressosController, Postgres Pré-condições: autenticada com alumni.list; cursoIds[] no JWT

<!-- image -->

Notas: Passo 4: JOIN entre usuario (role=EGRESSO) e graduation\_record (situacao\_diploma, data\_colacao); cursoIds[] restringe a egressos dos cursos de competência da secretária (RN-F5-005-01). Passo 6: \_links por item inclui confirm-delivery somente se situacao\_diploma=PENDENTE; item com ENTREGUE retorna apenas view - HATEOAS controla ações disponíveis por linha.

Exportação CSV: DRY → F5.8-D04-CSV (GET /secretaria/egressos?format=csv - mesmo padrão stream download) (RN-F5-005-05).

F5.11-D02-ELEGIVEIS - Buscar elegíveis para colação (GET /students?eligibleForGraduation=true) Escopo:  happy  path  -  secretária  inicia  wizard  de  colação;  backend  verifica  5  critérios  de elegibilidade por aluno Atores: Secretaria, WebApp, JwtFilter, GraduationController, Postgres

Pré-condições: autenticada com diploma.register; curso e período selecionados no formulário

<!-- image -->

Notas: Passo 4: o backend avalia os 5 critérios de elegibilidade por aluno em paralelo (RN-F5-005-07): (a) TCC aprovado, (b) todas as disciplinas do currículo concluídas, (c) horas\_formativas &gt;= curso.horas\_minimas,  (d)  sem  pendências  financeiras  (se  integração  ativa),  (e)  sem  solicitações bloqueantes abertas. Resultado materializado em eligible + bloqueio.razao (ex.: "Horas formativas insuficientes: 60/120 h"). Passo  6:  alunos  com  eligible:  false  retornam  bloqueio:  {razao,  detalhe}  -  frontend  desabilita  o checkbox e exibe tooltip com a razão (CA-F5-005-04, RN-F5-005-12). Sem HTTP extra.

Alunos já com role=EGRESSO (colação anterior) são excluídos da query.

F5.11-D03 -  Confirmar colação em  lote (POST  /graduations + TX: graduation\_record  + role → EGRESSO + outbox × N + audit\_log)

Escopo: happy path - secretária confirma colação de N alunos; TX atômica cria graduation\_record, transiciona usuario.role → EGRESSO e enfileira outbox\_event por egresso Atores: Secretaria, WebApp, JwtFilter, GraduationController, Postgres Pré-condições: autenticada com diploma.register; ao menos 1 aluno selecionado com eligible: true; dados da cerimônia preenchidos no Passo 2

<!-- image -->

Notas:

Passos 4-9: TX única para todos os N alunos - se qualquer INSERT/UPDATE falhar, o COMMIT não  ocorre  e  nenhum  aluno  é  promovido  (RN-F5-005-09). Evita estado parcial onde parte dos alunos seria EGRESSO e outra parte continuaria ALUNO.

Passo 6: UPDATE  usuario SET role=EGRESSO remove as capabilities do perfil  ALUNO (request.view\_own, event.attend, etc.) e concede as do perfil EGRESSO (alumni.view\_own). O JWT do aluno retém as capabilities antigas até expirar (15 min); em /egresso/inicio o frontend força re-autenticação via refresh token se detectar role ≠ EGRESSO (§5.2 F5.11b).

Passo 7: INSERT outbox\_event(egressos.graduated) × N - um evento por aluno; OutboxDispatcher processa  em  lote  e  envia  e-mail  de  boas-vindas  ao  portal  do  egresso  (RN-F5-005-10).  DRY → transversal/10.1-outbox-notificacao.md 10.1b para o dispatch completo.

Downstream (DRY): após a promoção, o aluno passa a ter acesso via GET /alumni/me (dashboard egresso) → F2/US-F2-001-DASHBOARD-EGRESSO.md F2.1-D01.

F5.11-D04 - Confirmar entrega física do diploma (PATCH confirm-delivery + audit\_log)

Escopo: happy path - secretária registra entrega do diploma impresso (presencial, procuração ou correio) após a colação

Atores: Secretaria, WebApp, JwtFilter, GraduationController, Postgres diploma.register; graduation\_record com Pré-condições: autenticada com situacao\_diploma=PENDENTE; \_link confirm-delivery presente

<!-- image -->

## Notas:

Passo 5: metodo\_entrega enum: {PRESENCIAL, PROCURACAO,  CORREIO}  (RN-F5-005-11); data\_entrega persiste como TIMESTAMPTZ.

Passo 6: audit\_log registra a evidência formal da entrega - relevante para compliance; campos: operadorId, metodo\_entrega, data\_entrega, graduation\_record\_id.

Passo 8: \_links após ENTREGUE: remove confirm-delivery, mantém view; possível revert-delivery com capability adicional (fora do escopo desta HU).

## F5.11-ERRO-01 - 403 FGAC: diploma.register ausente

Escopo: erro de autorização - usuário com alumni.list mas sem diploma.register tenta confirmar colação

Atores: Secretaria, WebApp, JwtFilter, GraduationController

Pré-condições: JWT válido; authorities incluem alumni.list mas não diploma.register

<!-- image -->

## Notas:

Passo 4: @PreAuthorize("hasAuthority('diploma.register')") bloqueia antes de qualquer query; RFC 7807 type=access\_denied (RN-F5-005-06). Observação HATEOAS: o botão "Confirmar colação" somente aparece se o \_link confirm estiver na resposta de GET /students?eligibleForGraduation=true - secretária sem diploma.register não vê o botão. O diagrama documenta a defesa backend para acesso direto à URL. Padrão idêntico a F5.6-ERRO-03 (DRY).

## HU 34. US-F5-006 - Revisão de Autorizações de Uso de Imagem

COMO secretária acadêmica QUERO revisar e aprovar ou rejeitar em lote as solicitações de autorização de uso de imagem dos alunos PARA eu possa processar esse volume alto de pedidos com eficiência sem precisar abrir cada solicitação individualmente.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Exibição compacta com thumbnails

DADO QUE

existem 30 solicitações AUTORIZACAO\_IMAGEM com estado ABERTA

QUANDO

a secretária acessa /secretaria/autorizacoes-imagem

ENTÃO

a tabela densa exibe as 30 solicitações com thumbnail de 48px, Nome, GRR, Curso, Data e Status o thumbnail exibe a foto enviada pelo aluno (URL pré-assinada MinIO)

Critério 2: Aprovação em lote DADO QUE existem 10 solicitações abertas de autorização de imagem QUANDO a secretária seleciona 8 delas e clica em "Aprovar lote" ENTÃO um dialog de confirmação é exibido com o número de itens selecionados QUANDO ela confirma ENTÃO a API recebe PATCH /requests/bulk-deliberate com os 8 IDs e decisão DEFERIDA as 8 linhas exibem status "Deferida" o Outbox emite notificação para cada aluno Critério 3: Falha parcial em lote DADO QUE a secretária aprova 5 solicitações em lote uma delas tem estado diferente de ABERTA (inconsistência de concorrência) QUANDO a API processa a ação ENTÃO a transação é revertida a UI exibe um AlertBanner listando o ID que causou a falha nenhuma das 5 solicitações é alterada Critério 4: Thumbnail expirado DADO QUE a URL pré-assinada de uma foto expirou

QUANDO

a tabela renderiza ENTÃO o placeholder de ícone de usuário é exibido no lugar do thumbnail o alt text é o nome do aluno para acessibilidade

Critério 5: Rejeitar com justificativa

DADO QUE a secretária seleciona 3 solicitações e clica em "Rejeitar lote" QUANDO o dialog pede uma justificativa opcional ela preenche "Foto ilegível" e confirma ENTÃO a API recebe PATCH /requests/bulk-deliberate com decisão INDEFERIDA e justificativa os 3 alunos recebem e-mail com a justificativa

DIAGRAMAS DE SEQUÊNCIA

F5.12-D01 - Listar solicitações compactas com thumbnails presigned (happy path)

Escopo:  happy  path  -  secretária  carrega  lista  compacta  de  autorizações  abertas;  backend  gera presigned URLs dos thumbnails MinIO Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres, MinIO

Pré-condições: autenticada com image\_authorization.review; solicitações type=AUTORIZACAO\_IMAGEM, estado=ABERTA existem

<!-- image -->

Notas:

Passo  4:  filtro  type=AUTORIZACAO\_IMAGEM  garante  que  somente  esse  tipo  aparece  na  tela (RN-F5-006-02); cursoIds[] restringe ao escopo da secretária; page\_size=50 permite tabela densa eficiente.

Passos 6-7: o RequestController gera as presigned URLs em batch antes de responder; TTL=15 min (RN-F5-006-03). Se foto\_storage\_key for null (aluno não enviou foto), thumbnail\_url retorna null - frontend exibe placeholder imediatamente sem tentar GET ao MinIO.

Passo 8: \_links.bulk\_deliberate presente apenas quando estado=ABERTA por item (RN-F5-006-08); itens  com  estado  diferente  retornam  sem  esse  \_link → checkbox  desabilitado  via  useActions. \_links global inclui bulk\_deliberate se ao menos 1 item for selecionável.

F5.12-D02 - Aprovar ou rejeitar em lote (PATCH bulk-deliberate + SELECT FOR UPDATE + TX × N + outbox × N)

Escopo:  happy  path  -  secretária  delibera  N  solicitações  abertas  em  lote;  TX  única  garante atomicidade; outbox notifica cada aluno

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições:  N  linhas  selecionadas  com  \_link  bulk\_deliberate;  todas  com  estado=ABERTA; confirmação no dialog

<!-- image -->

Notas:

Passo  4:  SELECT  ...  FOR  UPDATE  trava  as  N  linhas  antes  de  abrir  a  TX  -  previne  condição  de corrida com outro operador que possa ter alterado o estado entre a listagem e a deliberação. Se o count retornado for menor que N → desvia para F5.12-ERRO-01 (sem TX aberta).

Passos 6-10: TX única para todos os N itens (RN-F5-006-06); UPDATE + INSERT request\_event + INSERT  outbox\_event  +  INSERT  audit\_log  em  COMMIT  único. Se qualquer operação falhar → rollback automático → nenhuma solicitação é alterada.

Passo  8:  INSERT  outbox\_event  ×  N  -  um  evento  por  aluno  com  payload  {alunoId,  decisao, justificativa?}; template de e-mail AUTORIZACAO\_DELIBERATED inclui justificativa se INDEFERIDA (RN-F5-006-07, CA-F5-006-05). DRY → transversal/10.1-outbox-notificacao.md 10.1b para dispatch.

Rejeitar  com  justificativa  (CA-F5-006-05):  mesmo  fluxo  com  decisao=INDEFERIDA + justificativa: "Foto ilegível" no body; sem variação de participantes ou mensagens - DRY.

F5.12-ERRO-01 - 409 falha parcial por concorrência (estado mudou - TX não aberta)

Escopo:  erro  de  concorrência  -  ao  menos um dos IDs selecionados não está mais ABERTA no momento do PATCH; nenhuma alteração é aplicada

Atores: Secretaria, WebApp, JwtFilter, RequestController, Postgres

Pré-condições:  1  dos  N  IDs  teve  estado  alterado  por  outro  operador  entre  a  listagem  e  a confirmação

<!-- image -->

Notas:

Passo 5: o RequestController compara count(rows retornadas) com len(ids solicitados); divergência detectada antes de abrir  a  TX  -  zero  mutações  no  banco,  nenhum  rollback  necessário (RN-F5-006-06).

Passo  6:  RFC  7807  Problem  Details  type=bulk\_conflict;  body  inclui  failedIds[]  com  os  IDs divergentes. O frontend exibe DS/AlertBanner listando os IDs problemáticos (CA-F5-006-03).

Ação corretiva: o frontend recarrega a lista (invalidateQueries) para refletir o estado atualizado; a secretária pode refazer a seleção excluindo os itens em conflito.

## HU 35. US-F5-007 - Registro de Atendimento Presencial

COMO secretária acadêmica QUERO registrar atendimentos presenciais no guichê (aluno, assunto, resposta e anexo opcional) PARA

o histórico de atendimentos fique documentado no sistema, o aluno seja notificado e a secretaria possa consultar o histórico posteriormente.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Buscar aluno e preencher formulário

DADO QUE a secretária acessa /secretaria/atendimentos QUANDO ela digita "20231234" no Combobox ENTÃO o aluno "João Silva - GRR20231234" aparece na lista de sugestões QUANDO ela seleciona o aluno e preenche Assunto e Resposta ENTÃO o preview de notificação é atualizado em tempo real com as informações preenchidas

Critério 2: Registrar atendimento com anexo

DADO QUE a secretária preencheu o formulário e adicionou um PDF de 2 MB QUANDO ela clica em "Registrar" ENTÃO o arquivo é enviado ao MinIO a API recebe POST /service-records com o campo anexoUrl preenchido o aluno recebe e-mail com o resumo do atendimento e link para o histórico

Critério 3: Registrar atendimento sem anexo

DADO QUE

a secretária preencheu apenas Aluno, Assunto e Resposta QUANDO ela clica em "Registrar" ENTÃO a API recebe POST /service-records sem o campo anexoUrl o registro é criado com sucesso um toast de confirmação "Atendimento registrado" é exibido

Critério 4: Arquivo acima do limite

DADO QUE a secretária tenta anexar um PDF de 15 MB QUANDO ela arrasta o arquivo para o DS/FileDropzone ENTÃO o upload é bloqueado no frontend uma mensagem de erro "Arquivo excede 10 MB" é exibida

Critério 5: Preview de notificação

DADO QUE a secretária preencheu todos os campos QUANDO ela visualiza o preview de notificação ENTÃO o preview mostra o nome do aluno, o assunto selecionado e o texto da resposta um aviso indica "Este e-mail será enviado ao aluno ao confirmar"

DIAGRAMAS DE SEQUÊNCIA

- F5.13-D01 - Carregar formulário: categorias + busca aluno Combobox

Escopo: página carrega dropdown de categorias; secretária digita no Combobox e recebe sugestão de aluno Atores: Secretaria, WebApp, JwtFilter, ServiceController, Postgres Pré-condições: autenticada com service\_record.create

<!-- image -->

Notas: Passos 2-7: GET /service-record-categories é chamado na montagem da página; categorias são configuráveis pelo admin e raramente mudam - TanStack Query usa staleTime longo (ex.: 5 min) para evitar re-fetches desnecessários (RN-F5-007-03). Passos 9-14: busca Combobox com debounce 300 ms; DRY → F5/US-F5-002-SOLICITACOES.md F5.3-D02 passos 2-6. cursoIds[] restringe aos alunos dos cursos da secretária. Passo 14: ao selecionar o aluno, o preview de notificação (card estático) atualiza em tempo real via React state - sem HTTP adicional (RN-F5-007-05, CA-F5-007-05).

F5.13-D02 - Registrar atendimento com anexo (presigned PUT + POST + TX + outbox)

Escopo:  happy  path  -  secretária  confirma  formulário  com  PDF;  arquivo  vai  para  MinIO;  POST /service-records cria registro e dispara outbox Atores: Secretaria, WebApp, JwtFilter, ServiceController, MinIO, Postgres Pré-condições: autenticada com service\_record.create; campos obrigatórios preenchidos; arquivo PDF ≤ 10 MB validado client-side Notas:

<!-- image -->

Passos 2-8 (fase MinIO): padrão presigned PUT idêntico a F1.8-D03. O storage\_key retornado no passo 6 é incluído como anexoUrl no POST do passo 9; o arquivo já está no MinIO antes de criar o registro - objeto sem registro é removido por TTL-based cleanup.

Passo  11:  TX  atômica  -  INSERT  service\_record  +  INSERT  outbox\_event  em  COMMIT  único (padrão 10.1a). numero gerado atomicamente (AT-{ano}-{seq}).

Passo  14:  OutboxDispatcher  processa  atendimento.registrado → envia  e-mail  ao  aluno  com assunto, resposta e link para /meus-atendimentos (US-F1-011). DRY → transversal/10.1-outbox-notificacao.md 10.1b.

Sem anexo (CA-F5-007-03): passos 2-8 omitidos; POST /service-records sem storage\_key; mesmo TX e outbox - DRY, sem diagrama separado.

## HU 36. US-F5-008 - Gestão de Eventos Institucionais

criar  e  gerenciar  eventos formativos institucionais e operar o painel ao vivo (QR code ou PIN) no

COMO secretária acadêmica QUERO dia do evento PARA

as  presenças  dos  alunos  sejam  registradas  de  forma  controlada  e os certificados sejam emitidos automaticamente ao encerramento.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Listar e filtrar eventos

DADO QUE a secretária acessa /secretaria/eventos QUANDO a página carrega ENTÃO a tabela exibe os eventos dos cursos vinculados com Título, Período, Modo, Estado e Organizador os filtros de estado e curso estão disponíveis eventos CONCLUÍDOS não têm botões de Editar ou Excluir Critério 2: Criar novo evento DADO QUE a secretária clica em "Novo evento" QUANDO ela preenche Título, Datas, Modo QR\_SINGLE, Cursos vinculados define uma janela de validação de 09:00 às 10:00 ; salva ENTÃO a API recebe POST /events o evento aparece na lista com estado AGENDADO

Critério 3: Abrir painel de operação (QR\_SINGLE)

DADO QUE existe um evento com modo QR\_SINGLE no estado EM\_ANDAMENTO QUANDO a secretária acessa /secretaria/eventos/:id/operacao ENTÃO o painel exibe o QR Code da sessão em tela cheia um contador de presenças confirmadas é atualizado em tempo real via polling o botão "Encerrar evento" está disponível

Critério 4: Encerrar evento e gerar certificados

DADO QUE o evento foi operado com 30 alunos confirmados QUANDO a secretária clica em "Encerrar evento" ENTÃO a API recebe POST /events/:id/close o backend gera formative\_entry para os 30 alunos certificados são emitidos com hash SHA-256 e assinatura ED25519 o evento passa para estado CONCLUÍDO

Critério 5: Excluir evento com presença já registrada

DADO QUE um evento em AGENDADO já tem 1 registro de presença (inconsistência) QUANDO a secretária tenta excluí-lo ENTÃO a API retorna HTTP 422 "Evento possui registros de presença"

- a linha permanece na tabela

DIAGRAMAS DE SEQUÊNCIA

F5.8-D01 - Lista de eventos com filtros (scope secretaria)

Escopo: happy path - secretaria lista eventos de todos os cursos vinculados com filtros e \_links condicionais por estado Atores: Secretaria, WebApp, JwtFilter, EventController, ListEventsUseCase, Postgres Pré-condições: secretaria autenticada com event.manage; acessa /secretaria/eventos

<!-- image -->

## Notas:

Passo  5:  ListEventsUseCase  resolve  os  cursoIds  vinculados  à  secretariaId  internamente  -  sem onlyMine=true (RN-F5-008-02). A secretaria enxerga eventos de todos os cursos sob sua gestão, incluindo eventos criados por professores.

Passo  8:  HATEOAS  assembler  suprime  \_links.editar  e  \_links.excluir  para  estado=CONCLUIDO (RN-F5-008-05); \_links.host presente quando  event.host ✓ nas authorities da  secretaria. \_links.novoEvento sempre presente enquanto event.manage ✓ .

Filtros suportados: cursoId, estado  (AGENDADO|EM\_ANDAMENTO|CONCLUIDO),  onlyMine (boolean, opcional para filtrar apenas eventos da secretaria), page/size.

## F5.14-D02 - Encerrar evento: formative\_entry + outbox certificado

Escopo: secretaria encerra evento EM\_ANDAMENTO;  backend  processa presenças, gera formative\_entry em lote e enfileira emissão de certificados via outbox

Atores: Secretaria, WebApp, JwtFilter, EventController, CloseEventUseCase, Postgres

Pré-condições:  secretaria  com  event.host;  evento  EM\_ANDAMENTO;  \_links.encerrar-evento presente Notas: Passos 6-10: transação atômica - UPDATE event + INSERT formative\_entry (lote para todos os alunos  com  attendance\_session.completedAt  IS NOT NULL e chCreditadas atingidas) + INSERT outbox\_event  na  mesma  TX  (RN-F5-008-10).  Se  o  COMMIT  falhar,  nenhum formative\_entry é persistido e nenhum certificado é emitido. Passo 8: formative\_entry criado  apenas  para  alunos  que  atingiram  o  limiar  de  presença configurado no evento. Alunos sem presença válida não recebem entrada e, consequentemente, não recebem certificado. Passo 9: o OutboxDispatcher (a cada 5 s) lê events.closed e aciona CertificateIssuerUseCase para cada  formative\_entry  do  evento  -  emissão  assíncrona  completa  (PDF  +  SHA-256 + ED25519 + outbox) documentada em → transversal/10.4-certificado-emissao.md (RN-F5-008-11). RN-F5-008-12 (scheduler): @Scheduled aciona CloseEventUseCase.execute(eventId) automaticamente às 23:59 para eventos ainda EM\_ANDAMENTO - mesmo fluxo a partir do passo

<!-- image -->

5, sem ação da secretaria.

F5.8-ERRO - 422 excluir evento com presença já registrada

Escopo: secretaria tenta excluir evento AGENDADO que possui ao menos um registro de presença - operação rejeitada com 422 Atores: Secretaria, WebApp, JwtFilter, EventController, DeleteEventUseCase, Postgres Pré-condições:  JWT  válido  com  event.manage;  evento  estado=AGENDADO;  existe  ao  menos  1 attendance\_session para o evento (inconsistência de dados - CA-F5-008-05)

<!-- image -->

Notas: Passo  6:  o  DeleteEventUseCase  verifica  sequencialmente:  (a)  event.estado  =  AGENDADO  -  se EM\_ANDAMENTO  ou  CONCLUIDO → 409  Conflict;  (b)  ausência  de  attendance\_session  -  se count  &gt;  0 → 422  (RN-F5-008-06).  Ambas  as  condições  devem  ser  verdadeiras para o DELETE ocorrer. Passo  9:  RFC  7807  type=event\_has\_attendances,  status=422,  detail="Evento  possui  registros de presença". O frontend exibe DS/Toast error e a linha permanece na tabela (CA-F5-008-05). Em  condições  normais,  eventos  EM\_ANDAMENTO  e  CONCLUIDO  não  exibem  \_links.excluir (HATEOAS  cego).  O  422  é  defesa  em  profundidade  para  inconsistências  de  dados  ou  race conditions.

## HU 37. US-F5-009 - Importações em Lote

COMO

secretária acadêmica

QUERO

importar  dados  em  lote  (alunos,  disciplinas,  usuários,  alocação  de  professores)  via  planilha CSV/XLSX com preview de validação antes de confirmar

PARA

eu possa atualizar o cadastro massivamente no início do semestre sem erros silenciosos.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Baixar modelo

DADO QUE a secretária acessa /secretaria/importacoes QUANDO ela seleciona o kind "alunos" clica em "Baixar modelo" ENTÃO o sistema baixa o arquivo alunos\_modelo.csv com as colunas e linha de exemplo

Critério 2: Upload e validação

DADO QUE a secretária fez upload de alunos.csv com 500 linhas (480 válidas, 20 inválidas) QUANDO o backend conclui a validação (status VALIDATED) ENTÃO o preview exibe 480 linhas verdes e 20 linhas vermelhas com mensagens de erro por coluna o botão "Confirmar importação" está desabilitado uma mensagem "Corrija os 20 erros antes de confirmar" é exibida

Critério 3: Confirmar importação sem erros

DADO QUE a secretária corrigiu o arquivo e fez novo upload com 500 linhas todas válidas QUANDO ela clica em "Confirmar importação" ENTÃO

o backend processa em lotes de 1.000 linhas (1 lote neste caso) um relatório é exibido: "500 alunos importados com sucesso" o Outbox envia e-mail de sumário para a secretária

Critério 4: Processamento parcial

DADO QUE uma importação de 2.500 linhas processa o primeiro lote (1.000) com sucesso o segundo lote falha por erro de banco de dados ENTÃO o relatório exibe "1.000 importados, 1.500 não processados" o status do import\_job é PARTIAL o e-mail de sumário descreve o erro do segundo lote

Critério 5: Arquivo acima do limite

DADO QUE a secretária tenta fazer upload de um arquivo de 25 MB QUANDO arrasta o arquivo para o DS/FileDropzone ENTÃO o upload é bloqueado no frontend com mensagem "Arquivo excede 20 MB"

Critério 6: Auditabilidade DADO QUE a importação foi concluída com sucesso QUANDO o administrador consulta o audit\_log ENTÃO existe uma entrada com: operadorId, kind, timestamp, checksum, total\_linhas, status SUCCESS

DIAGRAMAS DE SEQUÊNCIA

F5.9-D01 - Baixar modelo CSV/XLSX

Escopo: happy path - secretaria seleciona kind e faz download do modelo gerado dinamicamente Atores: Secretaria, WebApp, JwtFilter, ImportController, ImportTemplateUseCase Pré-condições: secretaria autenticada com import.run; acessa passo 1 do wizard /secretaria/importacoes

<!-- image -->

Notas: Passo 5: ImportTemplateUseCase gera o modelo dinamicamente com as colunas obrigatórias, tipos e uma linha de exemplo (RN-F5-009-11). Não consulta dados reais - apenas o schema do kind. Resposta retorna o arquivo diretamente na resposta HTTP (inline bytes); nenhum MinIO envolvido para este endpoint. Kinds disponíveis (RN-F5-009-02): alunos, disciplinas, usuarios, alocacao\_professor. Mesmo fluxo para todos.

F5.9-D02 - Upload + validação assíncrona + polling (preview com erros)

Escopo: secretaria faz upload de CSV; backend cria import\_job + rows em TX e executa validação assíncrona; frontend faz polling até VALIDATED e exibe preview linha a linha Atores: Secretaria, WebApp, JwtFilter, ImportController, ValidateImportUseCase, Postgres Pré-condições:  secretaria  com  import.run;  arquivo  CSV/XLSX  ≤  20  MB  e  ≤  10.000  linhas (RN-F5-009-04).

<!-- image -->

Notas: Passo 9: "agenda validação assíncrona" - um @Async worker (ou @Scheduled) lê os import\_row com  status=PENDING  e  valida cada linha  (tipo  de  campo,  CPF,  GRR,  duplicata).  Atualiza import\_row.status  (VALID  /  WARNING  /  INVALID)  e  import\_row.errorMessage.  Ao  terminar, import\_job.status = VALIDATED. Loop (passos 12-13): o frontend repete o GET a cada ~2 s. Se status = FAILED (erro de parsing - ex.: XLSX corrompido), o polling termina e o WebApp exibe DS/AlertBanner error; \_links.confirm não é emitido. Passo 14: botão "Confirmar importação" só fica habilitado quando errorCount = 0 (RN-F5-009-07). Linhas com WARNING (amarelo) não bloqueiam - secretaria tem ciência antes de confirmar.

F5.9-D03 - Confirmar importação: lotes TX + audit\_log + outbox

Escopo: secretaria confirma importação sem erros; backend processa em lotes de 1.000, registra audit\_log e enfileira imports.completed via outbox Atores: Secretaria, WebApp, JwtFilter, ImportController, ConfirmImportUseCase, Postgres Pré-condições:  secretaria  com  import.run;  import\_job.status  =  VALIDATED;  errorCount  =  0; \_links.confirm presente

<!-- image -->

Notas:

Passos 6-11:  todos  dentro  da  mesma  TX  do  lote  1  (único  lote  para 500 linhas). Para importações com N &gt; 1.000 linhas, o ConfirmImportUseCase itera em lotes de 1.000 - cada lote tem sua própria TX (ver F5.9-ERRO-D04 para falha parcial). Passo  9:  INSERT  outbox\_event  do  tipo  imports.completed  na  mesma  TX  do  lote  final.  O OutboxDispatcher  (a  cada  5  s)  lê  o  evento  e  envia  e-mail  de  sumário  para  a  secretária  -  fluxo completo de dispatch em → transversal/10.1-outbox-notificacao.md. Passo 10:  audit\_log  registra  operadorId,  kind,  checksum  SHA-256  do  arquivo, totalLinhas, status final - base de auditabilidade (RN-F5-009-09, CA-F5-009-06).

F5.9-ERRO-D04 - Processamento parcial (TX lote 2 falha)

Escopo:  importação  de  2.500  linhas  processa  lote  1  com  sucesso,  lote  2  falha  (DB  error);  job encerrado como PARTIAL; lotes anteriores já confirmados permanecem Atores: Secretaria, WebApp, JwtFilter, ImportController, ConfirmImportUseCase, Postgres Pré-condições: secretaria com import.run; import\_job.status = VALIDATED; errorCount = 0; 2.500 linhas (3 lotes: 1.000 / 1.000 / 500)

<!-- image -->

Notas: Passo 6: lote 1 usa TX própria; ao concluir com COMMIT, os 1.000 registros estão persistidos e não são desfeitos por falhas subsequentes (RN-F5-009-08). Passo 7:  falha  no  lote  2  (ex.:  DataIntegrityViolationException  - duplicata ou FK inválida) provoca ROLLBACK  apenas  do  lote  2.  O  ConfirmImportUseCase  captura  a  exceção  e  interrompe  o processamento dos lotes restantes (lote 3 também não é executado). Passos 8-9: import\_job.status = PARTIAL + outbox\_event registrados em TX própria de finalização. O e-mail de sumário (via outbox) descreve o erro do lote 2 (RN-F5-009-10, CA-F5-009-04). Lotes processados com sucesso são irreversíveis - não há mecanismo de rollback de importação confirmada (fora de escopo da HU).

F5.9-ERRO-403 - 403 FGAC: import.run ausente

Escopo: usuário sem import.run tenta iniciar importação - acesso negado no JwtFilter Atores: Secretaria, WebApp, JwtFilter, ImportController Pré-condições: JWT válido; import.run ausente nas authorities do usuário Notas: Em condições normais, a rota /secretaria/importacoes não é acessível sem import.run (guarda de rota  no  frontend  também).  O  403  é  defesa  em  profundidade  contra  chamadas  diretas ou sessão com authority expirada. Passo 4: RFC 7807 type=access\_denied, status=403, detail="Authority import.run required". Corpo completo em Notas; inline apenas o shorthand.

<!-- image -->

## HU 38. US-F5-010 - Exportações Assíncronas

COMO secretária acadêmica QUERO solicitar exportações de dados em diferentes formatos (alunos, solicitações, presenças, certificados) e baixar os arquivos gerados de forma assíncrona PARA

eu possa obter relatórios volumosos sem travar o navegador enquanto eles são processados.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Solicitar exportação DADO QUE a secretária acessa /secretaria/exportacoes QUANDO

ela clica no card "Alunos" seleciona o filtro "Curso: TADS" e "Período: 2026/2" confirma ENTÃO a API recebe POST /exports/alunos com os filtros um job aparece no histórico com status PROCESSANDO um toast confirma "Exportação solicitada"

Critério 2: Polling e download

DADO QUE o export\_job muda para status PRONTO QUANDO o polling detecta a mudança ENTÃO o card no histórico exibe o botão "Baixar" ao clicar, o arquivo CSV é baixado via URL pré-assinada do MinIO a secretária recebe e-mail de notificação com o link de download

Critério 3: Link expirado

DADO QUE um export\_job tem mais de 7 dias QUANDO a secretária acessa o histórico ENTÃO o status desse job aparece como EXPIRADO o botão de download não está disponível o job ainda aparece por 30 dias para fins de auditoria

Critério 4: Acessibilidade do status assíncrono

DADO QUE

um job muda de PROCESSANDO para PRONTO QUANDO o polling atualiza a UI ENTÃO a região de status tem aria-live="polite" o leitor de tela anuncia "Exportação de Alunos pronta para download"

DIAGRAMAS DE SEQUÊNCIA

F5.17-D01 - Solicitar exportação (POST /exports/:kind → 202)

Escopo: happy path - secretaria seleciona kind, informa filtros e solicita exportação; backend cria export\_job assíncrono Atores: Secretaria, WebApp, JwtFilter, ExportController, CreateExportUseCase, Postgres Pré-condições: secretaria autenticada com export.run; acessa /secretaria/exportacoes

<!-- image -->

Notas: Passo 6: CreateExportUseCase persiste o export\_job com status=PROCESSANDO; não executa a geração do arquivo - isso é delegado ao ExportWorker assíncrono (ver F5.10-D02).

Passo  9:  o  frontend  não  bloqueia  após  o  202  (RN-F5-010-04).  A  UI  exibe  o  job  no  histórico imediatamente com DS/Badge(PROCESSANDO) e inicia o polling de 10 s (ver F5.10-D03). Kinds disponíveis (RN-F5-010-02): alunos, solicitacoes, presencas, certificados, egressos, formativas. Mesmo fluxo para todos.

F5.10-D02 - Job assíncrono: worker gera CSV → MinIO → outbox (fase TX)

Escopo:  ExportWorker  (background  -  @Scheduled)  processa  export\_job  PROCESSANDO:  gera CSV, faz PUT no MinIO e transiciona para PRONTO em TX atômica com outbox

Atores: ExportWorker, Postgres, MinIO Pré-condições: export\_job.status = PROCESSANDO; worker rodando a cada N segundos

<!-- image -->

Notas:

Passo 1: FOR UPDATE SKIP LOCKED evita que múltiplas instâncias do worker processem o mesmo job concorrentemente (padrão Outbox/Competing Consumers). Passo  5:  a  serialização  acontece  em  memória.  Para  volumes  muito  grandes  (N  &gt;  threshold), ExportWorker  pode  usar  streaming  direto  para  MinIO  via  multipart  upload  -  detalhe  de implementação; fluxo lógico idêntico. Passos 8-11:  TX  atômica  -  UPDATE export\_job(PRONTO) + INSERT outbox\_event(exports.ready) na mesma TX. Se o COMMIT falhar (ex.: queda após PUT MinIO), o worker não marcará o job como PRONTO e reprocessará na próxima execução (at-least-once). Passo 10: o OutboxDispatcher (a cada 5 s) lê exports.ready e envia e-mail com link de download - fluxo completo em → transversal/10.1-outbox-notificacao.md.

F5.10-D03 - Polling detecta PRONTO + download via presigned URL MinIO

Escopo: frontend faz polling e detecta job PRONTO; secretaria clica "Baixar" e recebe redirect para URL pré-assinada do MinIO (15 min) Atores: Secretaria, WebApp, JwtFilter, ExportController, Postgres, MinIO Pré-condições: secretaria com export.run; export\_job.status transicionou para PRONTO (via D02)

<!-- image -->

Notas: Loop  (passos  1-4):  polling  suspenso  quando  não  há  mais  jobs  PROCESSANDO na resposta (lista vazia  ou  todos  PRONTO/EXPIRADO).  O  frontend  usa  aria-live="polite"  ao  atualizar  o  status (CA-F5-010-04 - detalhe de acessibilidade, sem chamada HTTP adicional). Passo 11:  ExportController  verifica  expiresAt  &gt;  now()  antes  de  chamar  MinIO. Se expirado → 410 Gone  (defesa  em  profundidade;  em  condições  normais,  \_links.download  já  estaria ausente por HATEOAS). Passo 13: a URL pré-assinada tem TTL de 15 min (curto por segurança); o arquivo MinIO tem TTL de 7 dias (RN-F5-010-06), gerenciado pelo scheduler (F5.10-D04). CA-F5-010-03  (status  EXPIRADO):  o  mesmo  GET  /exports retorna status: EXPIRADO para jobs com expiresAt &lt; now() - \_links.download ausente (HATEOAS cego); sem diagrama separado. DRY.

F5.10-D04 - Scheduler expira job: DELETE MinIO + UPDATE EXPIRADO

Escopo:  ExportScheduler  (@Scheduled)  detecta  export\_jobs  com  expiresAt  &lt;  now(),  remove arquivo do MinIO e marca status como EXPIRADO Atores: ExportScheduler, Postgres, MinIO Pré-condições: export\_job.status = PRONTO; expiresAt &lt; now() (≥ 7 dias desde criação)

<!-- image -->

Notas: Passo 1: SKIP LOCKED garante que instâncias concorrentes do scheduler não processem o mesmo lote.  O  scheduler  roda  periodicamente  (ex.:  a  cada  hora  ou  diariamente  -  frequência  de configuração). Passos 3-4: DELETE do arquivo no MinIO ocorre antes do UPDATE no banco. Se o DELETE falhar, o  job  permanece PRONTO e será reprocessado na próxima execução (idempotente). Se o banco falhar  após  o  DELETE,  o  arquivo  já  foi  removido  e  o  job  será  marcado  EXPIRADO  na  próxima rodada com uma verificação de existência no MinIO. Passo 6: status = EXPIRADO não exclui o registro - ele permanece visível no histórico por 30 dias para  fins  de  auditoria  (RN-F5-010-09).  Exclusão  física  do  export\_job  ocorre  após  30  dias  (job separado ou coluna purge\_after).

CA-F5-010-03: após este scheduler, o GET /exports retorna status: EXPIRADO sem \_links.download - comportamento coberto em F5.10-D03 (mesma listagem, estado diferente).

F5.10-ERRO-403 - 403 FGAC: export.run ausente

Escopo: usuário sem export.run tenta solicitar exportação - acesso negado no JwtFilter Atores: Secretaria, WebApp, JwtFilter, ExportController Pré-condições: JWT válido; export.run ausente nas authorities do usuário

<!-- image -->

Notas:

Em condições normais, a rota /secretaria/exportacoes não é acessível sem export.run (guarda de rota frontend + @PreAuthorize). O 403 é defesa em profundidade.

Passo 4: RFC 7807 type=access\_denied, status=403, detail="Authority export.run required".

## HU 39. US-F5-011 - Estatísticas da Secretaria

COMO

secretária acadêmica

QUERO

visualizar  dashboards  quantitativos  com  gráficos  de  solicitações,  presenças  e  horas  formativas filtrados por período e curso

PARA

eu  possa  monitorar  indicadores  operacionais  e  identificar  tendências  sem  precisar  exportar planilhas manualmente.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Exibição de gráficos com filtros DADO QUE a secretária acessa /secretaria/estatisticas QUANDO ela seleciona o filtro "Período: 2026/2" e "Curso: TADS" ENTÃO a API recebe GET /reports/secretary?periodo=2026-2&amp;curso=TADS os 4 gráficos são renderizados com os dados filtrados as cores dos gráficos usam os tokens do design system Critério 2: Drill-down ao clicar em gráfico DADO QUE o gráfico de "Solicitações por tipo" está exibido QUANDO a secretária clica na barra "Declaração de vínculo" ENTÃO a tabela de drill-down abaixo é atualizada com as solicitações desse tipo a tabela é paginada com 20 registros por página Critério 3: Estado skeleton durante carregamento a secretária aplica um filtro diferente

DADO QUE QUANDO a requisição está em andamento ENTÃO

cada área de gráfico exibe DS/Skeleton com animação de pulso a tabela de drill-down exibe linhas skeleton Critério 4: Acessibilidade dos gráficos DADO QUE os gráficos estão exibidos QUANDO um leitor de tela navega pela página ENTÃO cada gráfico tem uma região com resumo textual descrevendo o dado mais relevante o título do gráfico está em um elemento com role="heading" Critério 5: Persistência de filtros na URL DADO QUE a secretária seleciona os filtros "2026/2" e "TADS" QUANDO ela copia a URL e abre em outra aba ENTÃO a nova aba carrega os mesmos filtros aplicados DIAGRAMAS DE SEQUÊNCIA F5.11-D01 - GET /reports/secretary (cache MISS - filtros + 4 datasets) Escopo: happy path - secretaria acessa /secretaria/estatisticas com filtros; TanStack Query não tem cache; backend agrega 4 datasets e retorna payload único Atores: Secretaria, WebApp, JwtFilter, ReportController, SecretaryReportUseCase, Postgres Pré-condições: secretaria autenticada com report.view\_secretary; cache TanStack Query ausente ou expirado (&gt; 5 min)

<!-- image -->

Notas: Passo 6: SecretaryReportUseCase executa 4 queries agregadas com os filtros recebidos (periodoId, cursoId).  Podem ser executadas em paralelo (coroutines) ou via CTE para reduzir round-trips. O cursoId=null retorna dados de todos os cursos vinculados à secretariaId (RN-F5-011-02). Passo 10: TanStack Query armazena o resultado com chave ['reports', 'secretary', { periodo, curso }] e staleTime=5min. Enquanto não expirado, novas navegações para a mesma URL servem do cache (ver F5.18-D02). CA-F5-011-02 (drill-down): após o render, clicar em barra/fatia filtra client-side o array do dataset correspondente  e  renderiza  a  DS/DataTable  com  paginação  de  20  por  página  -  sem  HTTP adicional. DRY. CA-F5-011-05  (URL):  params  ?periodo=2026-2&amp;curso=TADS  são  lidos  pela  URL  ao  montar  o componente  e  passados  como  defaultValues  dos  selects  +  como  parâmetros  do  GET.  Mesma chamada deste diagrama.

F5.18-D02 - Cache HIT + refresh manual Escopo: secretaria retorna à tela dentro de 5 min (TanStack Query serve do cache sem HTTP); e cenário de refresh manual que invalida o cache e aciona novo fetch Atores: Secretaria, WebApp Pré-condições: cache TanStack Query presente e não expirado (staleTime=5min não atingido)

<!-- image -->

Notas:

Passo 2: staleTime=5min garante que não haja HTTP enquanto os dados são considerados frescos. Após expirar, TanStack Query revalida automaticamente em background (background refetch) sem mostrar Skeleton - o usuário continua vendo os dados antigos até a resposta chegar. Passo 5: invalidateQueries  força  stale=true  imediatamente,  aciona  um  novo  fetch  e  exibe DS/Skeleton (RN-F5-011-08) enquanto o refetch de F5.11-D01 está em voo. Funcional em todos os filtros da chave de cache. Este diagrama documenta o padrão de cache para leitura pura (sem mutação); aplica-se igualmente ao análogo F6.2 coordenação (GET /reports/coordinator).

F5.11-ERRO-403 - 403 FGAC: report.view\_secretary ausente

Escopo: usuário sem report.view\_secretary tenta acessar o endpoint de estatísticas Atores: Secretaria, WebApp, JwtFilter, ReportController Pré-condições: JWT válido; report.view\_secretary ausente nas authorities

<!-- image -->

Notas:

Em condições normais, a rota frontend /secretaria/estatisticas já tem guarda de capability; o 403 é defesa em profundidade para calls diretas ou authority expirada na sessão. Passo 4: RFC  7807 type=access\_denied,  status=403,  detail="Authority  report.view\_secretary required".

## HU 40. US-F5-012 - Tarefas Internas da Secretaria

COMO secretária acadêmica QUERO gerenciar uma lista de tarefas internas no estilo kanban (pendente / concluída) PARA eu possa organizar afazeres do dia a dia da secretaria sem recorrer a ferramentas externas.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Feature flag desativada

DADO QUE

a feature flag tasks.enabled está desativada

QUANDO

a secretária tenta acessar /secretaria/tarefas

ENTÃO

o sistema retorna 404 ou redireciona para o dashboard

o item de menu "Tarefas" não é exibido na navegação

Critério 2: Criar nova tarefa

DADO QUE tasks.enabled está ativa e a secretária acessa /secretaria/tarefas QUANDO ela clica em "Nova tarefa" preenche título "Enviar memorando SCA" com vencimento em 2 dias salva ENTÃO a API recebe POST /tasks o card aparece na coluna "Pendentes"

Critério 3: Mover para concluída via drag-and-drop

DADO QUE existe um card "Enviar memorando SCA" em Pendentes QUANDO a secretária arrasta o card para a coluna Concluídas ENTÃO a API recebe PATCH /tasks/:id com estado CONCLUIDA o card aparece na coluna Concluídas

Critério 4: Alternativa por teclado (acessibilidade)

DADO QUE um card está em Pendentes QUANDO a secretária navega até o botão "Concluir" via Tab e pressiona Enter ENTÃO o estado da tarefa é atualizado para CONCLUIDA o card move para a coluna Concluídas Critério 5: Vencimento ultrapassado DADO QUE

uma tarefa tem data de vencimento ontem

QUANDO

o kanban é exibido

ENTÃO

a data de vencimento aparece em vermelho (status/danger) no card

DIAGRAMAS DE SEQUÊNCIA

F5.19-D01 - Carregar kanban (happy path)

Escopo:  GET  /tasks  -  lista  tarefas  PENDENTE  e  CONCLUIDA  da  secretária  logada;  renderiza kanban 2 colunas.

Pré-condições: JWT válido; task.manage presente; tasks.enabled=true.

<!-- image -->

Notas: Passo 4: query retorna todas as tarefas do responsavel\_id; o UseCase particiona em pendentes[] e concluidas[] no DTO de resposta. \_links:  [criar]  exposto  apenas  se  task.manage  confirmado  -  frontend  usa  useActions(resource) para exibir o botão "Nova tarefa". Feature  flag  tasks.enabled  verificada  no  frontend  (guard  de  rota  React  Router,  oculta  item  de

menu) e no backend como segunda barreira (ver F5.19-ERRO-01).

F5.19-D02 - Criar tarefa (happy path)

Escopo: POST /tasks - secretária cria nova tarefa; card aparece na coluna "Pendentes". Pré-condições: JWT válido; task.manage presente; título entre 1-200 chars.

<!-- image -->

Notas: Passos 4-6: transação atômica; sem outbox (tarefas internas - sem notificações externas no MVP, RN-F5-012-08). estado é sempre PENDENTE no INSERT - nunca lido do body da requisição. \_links:  [excluir]  presente  apenas  para  estado='PENDENTE'; backend controla via HATEOAS (ver F5.19-D04).

F5.19-D03 - Atualizar estado - PATCH /tasks/:id (happy path)

Escopo: PATCH /tasks/:id - mover tarefa entre colunas; cobre CA-F5-012-03 (drag-and-drop) e CA-F5-012-04 (botão teclado) com a mesma chamada de API. Pré-condições: JWT válido; task.manage presente; tarefa existe e pertence ao responsável.

<!-- image -->

Notas:

CA-F5-012-03 (drag) e CA-F5-012-04 (teclado) fazem a mesma chamada PATCH /tasks/:id {estado} - diferença está apenas no evento de UI que a dispara (DRY).

Mover  de  "Concluídas" → "Pendentes"  usa  {estado:  "PENDENTE"}  no  mesmo  endpoint;  \_links: [reabrir] ativa esse fluxo.

Resposta 200 omite \_links: [excluir] quando estado='CONCLUIDA' - backend controla visibilidade via HATEOAS.

F5.19-D04 - Excluir tarefa (DELETE /tasks/:id)

Escopo: DELETE /tasks/:id - secretária exclui tarefa PENDENTE; guard backend impede exclusão de CONCLUIDA.

Pré-condições: JWT válido; task.manage presente; tarefa em estado='PENDENTE'.

<!-- image -->

Notas:

SELECT antes do DELETE garante o guard de estado; se estado='CONCLUIDA', lança BusinessRuleException → 409 (ver F5.19-ERRO-04). Frontend  oculta  o  botão  "Excluir"  para  tarefas  CONCLUIDA  via  ausência  de  \_links:  [excluir] (HATEOAS - defense-in-depth). Soft-delete não aplicável: tarefas internas sem histórico auditável exigido por compliance no MVP.

F5.19-ERRO-01 - Feature flag desativada (404)

Escopo: tasks.enabled=false → backend retorna 404; frontend oculta item de menu e redireciona. Origem: CA-F5-012-01 · RN-F5-012-01.

<!-- image -->

Notas: Frontend verifica tasks.enabled no guard de rota React Router antes de renderizar a tela (primeira barreira); chamada ao backend é a segunda barreira. Item "Tarefas" não incluído em \_links do dashboard quando flag inativa - useActions não exibe o link. Feature flag controlada via variável de ambiente ou tabela feature\_flags; sem hot-reload no MVP.

F5.19-ERRO-02 - 403 FGAC (task.manage ausente)

Escopo: usuário autenticado sem capability task.manage tenta qualquer endpoint /tasks. Origem: RN-F5-012-02.

## Notas:

@PreAuthorize("hasAuthority('task.manage')") no TaskController intercepta antes da execução do UseCase.

Mesmo guard aplica-se a POST, PATCH e DELETE /tasks.

Perfis aluno, professor e coordenador nunca recebem task.manage (RN-F5-012-08).

F5.19-ERRO-03 - 422 Validação (POST /tasks - título inválido)

Escopo: título vazio ou com mais de 200 chars → 422 Problem Details; inline error no campo. Origem: RN-F5-012-03.

<!-- image -->

## Notas:

Validação Jakarta (@NotBlank, @Size(max=200)) no DTO dispara antes do UseCase em produção; diagrama mostra o caminho semântico via UseCase para rastreabilidade de responsabilidades. WebApp pré-valida com Zod (z.string().min(1).max(200)) antes de enviar - 422 é a defesa backend.

<!-- image -->

descricao e responsavelId são opcionais; vencimento também opcional (RN-F5-012-03).

F5.19-ERRO-04 - 409 DELETE tarefa CONCLUIDA

Escopo:  tentativa  de  excluir  tarefa  no  estado  CONCLUIDA → 409  Conflict  (guard  de  regra  de negócio). Origem: API contract DELETE /tasks/:id / / somente tarefas PENDENTE.

<!-- image -->

Notas: Fluxo normalmente inacessível pela UI (HATEOAS oculta \_links: [excluir] para CONCLUIDA); este diagrama documenta a defesa backend. Para excluir uma  tarefa concluída, a secretária deve primeiro reabri-la (PATCH  {estado:

"PENDENTE"}) e então excluí-la.

## Execução fila Item: US-F5-012 Status: pendente → feito Arquivo: sequenceDiagrams/F5/US-F5-012-TAREFAS.md Próximo: US-F6-001

## 8.7 COORDENAÇÃO

## HU 41. US-F6-001 - Configurar Parâmetros do Curso

## COMO

coordenador de curso

## QUERO

configurar  os  parâmetros  curriculares  do  meu  curso  (horas  formativas  mínimas,  duração  do calendário letivo, regras da banca de TCC e texto do regimento)

## PARA

o  sistema  aplique  automaticamente  as  regras  corretas  de  elegibilidade,  SLA  de  deliberação  e composição de bancas sem intervenção manual da secretaria.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Exibição dos valores atuais DADO QUE o coordenador acessa /coordenacao/cursos/tads/configurar QUANDO a tela carrega ENTÃO os campos exibem os valores atuais persistidos no banco: horas formativas, duração calendário, membros externos banca, modalidade banca, regimento o botão "Salvar" está desabilitado (nenhum campo alterado) Critério 2: Alterar horas formativas mínimas DADO QUE o valor atual de horas formativas é 120 QUANDO o coordenador altera para 150 e clica em "Salvar" ENTÃO a API recebe PATCH /courses/tads/config com { "horasFormativasMinimas": 150 } o audit\_log registra { campo: "horasFormativasMinimas", de: 120, para: 150 } um toast "Configuração salva" é exibido Critério 3: Dirty state e cancelar com confirmação DADO QUE o coordenador alterou o número de membros externos de 1 para 2 QUANDO ele clica em "Cancelar"

ENTÃO um dialog de confirmação pergunta "Deseja descartar as alterações?"

QUANDO ele confirma ENTÃO o campo retorna ao valor anterior (1) sem chamar a API

Critério 4: Restrição ao curso próprio

DADO QUE o coordenador é responsável apenas pelo curso TADS QUANDO ele tenta acessar /coordenacao/cursos/ec/configurar (Engenharia de Computação) ENTÃO a API retorna HTTP 403 a UI exibe AlertBanner "Você não é coordenador deste curso"

Critério 5: Não-retroatividade de horas formativas

DADO QUE o aluno "João" tem 125 horas formativas validadas com limiar anterior de 120 QUANDO o coordenador aumenta o limiar para 150 ENTÃO o aluno João mantém o status de elegível para colação apenas novos cálculos de elegibilidade usam o limiar 150

Critério 6: Validação de campos

DADO QUE o coordenador insere -10 no campo de horas formativas mínimas QUANDO

tenta salvar ENTÃO o campo exibe erro inline "Valor deve ser entre 0 e 1000" o botão "Salvar" permanece desabilitado

DIAGRAMAS DE SEQUÊNCIA

F6.1-D01 - Carregar configuração do curso (happy path)

Escopo: happy path - coordenador abre a tela; WebApp dispara GET /courses/:id/config; valores persistidos populam o formulário. Pré-condições: Coordenador autenticado com JWT válido e capability course.config. Coordenador é responsável pelo curso (course.coordenador\_id = usuario.id).

<!-- image -->

Notas: Passo  3:  GetCourseConfigUC  verifica  course.coordenador\_id  =  userId  antes  de  prosseguir;  se falhar → CourseOwnershipException (ver F6.1-ERRO). Passo  7:  \_links.update  (rel  course:update-config)  presente  apenas  quando  o  coordenador  tem capability course.config - o frontend usa useActions(resource) para habilitar o formulário. Botão  "Salvar"  habilitado  somente  quando  isDirty  =  true  (RN-F6-001-09  -  comportamento frontend puro).

F6.1-D02 - Salvar configuração (PATCH + audit\_log)

Escopo:  happy  path  de  escrita  -  coordenador  altera  um  ou  mais  campos  e  confirma;  PATCH /courses/:id/config persiste os campos dirty em transação atômica com audit\_log.

Pré-condições: Formulário carregado via F6.1-D01 com \_links.update presente. Ao menos um campo difere do valor persistido (isDirty = true).

<!-- image -->

Notas: Passos 5-9: transação atômica (@Transactional). O SELECT … FOR UPDATE lê o valor anterior para construir o diff do audit\_log (RN-F6-001-06) e aplica o UPDATE na mesma TX. Se múltiplos campos forem alterados simultaneamente, o INSERT audit\_log pode ser multi-row (um por campo modificado) dentro da mesma transação - detalhe de implementação, não altera o diagrama. Não-retroatividade  (RN-F6-001-07):  ao  atualizar  horasFormativasMinimas,  o  UC  não  recalcula elegibilidades  existentes.  Alunos  já  elegíveis  pelo  limiar  antigo  permanecem  elegíveis;  novos cálculos usarão o valor 150. Backend valida horasFormativasMinimas ∈ [0, 1000] (422 defense-in-depth) mesmo que o frontend impeça o submit com valor inválido. Campos opcionais na RN: duracaoCalendario (15/18 SEMANAS), bancaMembrosExternos (1 ou 2), bancaModalidade  (PRESENCIAL/REMOTO/HÍBRIDO),  regimento  (máx.  10.000  chars)  -  todos

seguem o mesmo padrão PATCH desta transação.

F6.1-ERRO - 403 FGAC: coordenador acessa curso alheio

Escopo:  erro  de  autorização  -  coordenador  tenta  GET ou PATCH do config de um curso cujo coordenador\_id não corresponde ao seu userId. Pré-condições:

Coordenador autenticado com JWT válido e capability course.config (Spring Security passa). course.coordenador\_id ≠ userId para o curso requisitado.

<!-- image -->

Notas: Passo 2:  JwtFilter  valida JWT e Spring Security confirma capability course.config antes de chegar ao  controller  -  esse  pré-filtro  não  aparece  no  diagrama  pois  não  causa  403  neste  cenário  (o coordenador tem a capability global; a restrição é de escopo de dado). O mesmo guard de ownership se aplica ao PATCH /courses/ec/config - o UpdateCourseConfigUC executa a mesma verificação antes de iniciar a TX. RFC 7807 Problem Details no corpo: { "type": "course\_ownership\_denied", "status": 403, "detail": "…" } - não inline para não clipar o SVG.

## HU 42. US-F6-002 - Relatórios Analíticos de Coordenação

COMO coordenador de curso QUERO visualizar  relatórios  analíticos  com  séries  históricas  de  evasão,  validação  de  horas  formativas, comparativo entre turmas e carga de deliberadores PARA eu  possa  tomar  decisões  acadêmicas  baseadas  em  dados  e  identificar  tendências  que  exigem intervenção antecipada. DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Carregamento e estado Loading DADO QUE o coordenador acessa /coordenacao/relatorios QUANDO a requisição está em andamento ENTÃO

o frame Loading (721:1209) é exibido:

DS/Skeleton/block em cada uma das 4 áreas de gráfico os KpiCards exibem placeholders animados

Critério 2: Exibição de KPIs e gráficos

DADO QUE o coordenador aplica os filtros "Período: 2026/2" e "Curso: TADS" QUANDO a resposta da API é recebida ENTÃO os 4 KpiCards exibem: tempo médio, taxa indeferimento, horas validadas, taxa presença os 4 gráficos são renderizados com dados do período 2026/2 para TADS as cores dos gráficos usam tokens do design system (sem hex hardcoded)

Critério 3: Alerta de taxa de indeferimento

DADO QUE o threshold de indeferimento configurado para TADS é 20% a taxa atual do período é 35% QUANDO os relatórios carregam ENTÃO um DS/AlertBanner de aviso aparece no topo da coluna esquerda: "Taxa de indeferimento: 35% (acima do limite 20%)" o KpiCard de taxa de indeferimento exibe a cor status/danger

Critério 4: Seção Pendências clicável

DADO QUE existem 2 bancas de TCC sem composição definida QUANDO o coordenador visualiza a seção Pendências ENTÃO

aparecem 2 DS/PendenciaItem com descrição "Banca sem composição - [título do TCC]" QUANDO ele clica em um item ENTÃO é redirecionado para a tela de composição da banca

Critério 5: Gráfico Evasão por período (série histórica)

DADO QUE existem dados de 4 semestres anteriores para TADS QUANDO o gráfico (1,1) "Evasão por período" renderiza ENTÃO exibe uma linha de alunos ativos e outra de evadidos para cada semestre o eixo X mostra os rótulos dos semestres (ex.: "2024/1", "2024/2", "2026/1", "2026/2") um resumo textual acessível descreve o ponto de maior evasão

Critério 6: QuickTiles HATEOAS

DADO QUE a resposta da API não contém \_link para "tipos-solicitacao" QUANDO a seção de QuickTiles renderiza ENTÃO o tile "Tipos de Solicitação" não é exibido os 5 tiles restantes com \_links disponíveis aparecem normalmente

Critério 7: Filtros persistem na URL

DADO QUE o coordenador aplica "Período: 2026/2" e "Curso: TADS" QUANDO ele copia a URL e abre em nova aba ENTÃO a nova aba carrega os mesmos filtros aplicados Critério 8: Drill-down de carga por deliberador DADO QUE o coordenador clica no gráfico (2,1) "Comparativo entre cursos" QUANDO a tabela de drill-down atualiza ENTÃO ela exibe por deliberador: nome, quantidade de deliberações, tempo médio (dias) a tabela é paginada com 20 registros por página.

DIAGRAMAS DE SEQUÊNCIA

F6.2-D01 - GET /reports/coordinator (cache MISS, com filtros)

Escopo:  happy  path  -  coordenador  acessa  /coordenacao/relatorios  com  filtros  aplicados; TanStack Query não tem entrada em cache (primeira carga ou staleTime expirado); backend agrega KPIs, séries históricas e dados operacionais em resposta única. Pré-condições: Coordenador autenticado com JWT válido e capability report.view\_coordinator. TanStack Query: cache MISS para a chave ['coordinator-report', filters]. Filtros aplicados via query string: ?periodo=2026-2&amp;curso=TADS.

<!-- image -->

Notas: Passo 2: enquanto o GET está em voo, WebApp exibe DS/Skeleton/block em cada área de gráfico e placeholders animados nos KpiCards (CA-F6-002-01) - estado isLoading=true do TanStack Query; não é uma chamada de rede separada. Passos  5-10:  três  rounds  de  queries  por  separação  semântica.  Na  implementação,  o  UC  pode executar em  paralelo (async/coroutines) ou como  single CTE; o diagrama representa a granularidade de agregação, não obrigatoriamente queries sequenciais. Passo 12:  thresholdIndeferimento chega junto com kpis; o frontend compara taxaIndeferimento &gt; thresholdIndeferimento para exibir o DS/AlertBanner (CA-F6-002-03 - lógica frontend pura). Passo  12:  \_links  condicional  por  FGAC;  useActions(resource)  controla  quais  DS/QuickTile  são renderizados (CA-F6-002-06, RN-F6-002-11).

Passo 12: pendencias[].href contém o destino de navegação para cada pendência; o clique dispara navigate(href) sem nova API call (CA-F6-002-04). CA-F6-002-07  (filtros na URL): quando  o usuário altera qualquer DS/Select  de  filtro, o useSearchParams atualiza a URL e TanStack Query invalida a chave, disparando um novo GET com os query params atualizados - mesmo fluxo deste diagrama. CA-F6-002-08 (drill-down): cargaPorDeliberador[] já está no response (passo 10 → 12); a tabela de drill-down renderiza esses dados localmente ao clicar no gráfico, sem nova requisição. Não há OUTBOX, CERT ou WORKFLOW neste fluxo - HU é read-only.

- 8.8 ADMIN

## HU 43. US-F7-001 - Gestão de Usuários e Reset de Senha (Administrativo)

COMO administrador da plataforma QUERO gerenciar todos os usuários do sistema (criar, editar, desativar, atribuir perfis) e disparar reset de senha por link de uso único PARA o  acesso  ao  sistema  esteja  sempre  correto  e  seguro,  sem  que  eu  precise  manipular  senhas diretamente.

DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

<!-- image -->

## [INSERIR TELA MOBILE AQUI]

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Listar e filtrar usuários

DADO QUE o admin acessa /admin/usuarios QUANDO a tabela carrega ENTÃO

todos os usuários do sistema são exibidos com Nome, E-mail, Tipo, Situação, Último acesso a busca filtra por nome (trigrama), e-mail e GRR em tempo real (debounce 300 ms) Critério 2: Criar usuário DADO QUE o admin clica em "Novo" QUANDO preenche Nome, E-mail, Tipo e Curso (se aplicável) e salva ENTÃO a API cria o usuário com mustChangePassword=true o usuário recebe e-mail com link de acesso inicial via Outbox o admin nunca visualiza a senha criada Critério 3: Desativar usuário DADO QUE o admin clica em "Desativar" na linha de um usuário ativo QUANDO confirma o dialog destrutivo ENTÃO a API atualiza status=INATIVO via PATCH /admin/usuarios/:id todos os tokens JWT do usuário são invalidados via JTI blacklist a linha exibe badge "Inativo" o audit\_log registra a ação Critério 4: Reset de senha (F7.8) - operador nunca vê senha DADO QUE o admin clica em "Reset senha" na linha de um usuário QUANDO o modal DS/Dialog · Reset senha é exibido ENTÃO

o modal mostra apenas o nome do usuário e um botão "Confirmar envio" não há nenhum campo de senha ou exibição de credencial QUANDO o admin confirma ENTÃO a API recebe POST /users/:id/password-reset o usuário recebe e-mail com link JWT 1-uso válido por 24 h o DS/AlertBanner informativo é exibido: "Link enviado para [email]"

Critério 5: Link de reset expirado

DADO QUE um link de reset foi enviado há mais de 24 h e não foi usado QUANDO o usuário tenta usá-lo ENTÃO a API retorna HTTP 401 "Token expirado" a UI de redefinição (F0.3) exibe mensagem de erro com link para solicitar novo reset

Critério 6: HATEOAS: ações condicionais

DADO QUE um usuário tem status INATIVO QUANDO o admin visualiza sua linha na tabela ENTÃO o botão "Desativar" não é exibido (rel "deactivate" ausente) apenas os botões "Editar" e "Reset senha" estão disponíveis via \_links

DIAGRAMAS DE SEQUÊNCIA

F7.1-D01 - Listar e filtrar usuários (happy path)

Escopo: happy path - admin acessa /admin/usuarios; API retorna página de usuários com \_links condicionais por item Atores: Admin, WebApp, IAMController, ListUsersUseCase, Postgres Pré-condições: admin autenticado com capability user.manage\_all

<!-- image -->

Notas: JwtFilter valida Bearer e verifica user.manage\_all antes de chegar ao controller (inline no label do passo 2) deactivate  rel  ausente  na  resposta  para  usuários  com  status=INATIVO  (RN-04;  CA-06 → DRY acima) Busca  por  trigrama  (nome),  prefix  (e-mail),  exato  (GRR/matrícula)  com  debounce  300  ms  no frontend (RN-03) Paginação  padrão 20/página; ordenação por Nome ou Último acesso suportada via sort= query param (RN-10) Lacunas: nenhuma F7.1-D02 - Criar usuário (POST + mustChangePassword + outbox) Escopo:  happy  path  -  admin  cria  novo  usuário;  sistema  gera  senha  temporária  (Argon2id)  e

enfileira e-mail de acesso via Outbox Atores: Admin, WebApp, IAMController, CreateUserUseCase, Postgres Pré-condições: admin com user.manage\_all; e-mail alvo inexistente no sistema Notas: Admin nunca visualiza a senha temporária gerada (RN-05) - hash Argon2id persiste diretamente mustChangePassword=true força troca na primeira autenticação (fluxo US-F1-002) Fase de dispatch do e-mail (OutboxDispatcher → MailAdapter) → transversal/10.1 audit\_log registra operadorId, targetUserId, acao, timestamp, payload (RN-09) Se e-mail já existir → 409 Conflict (capturado via SELECT BY email antes da TX; diagrama de erro omitido - padrão idêntico a F5.6-ERRO-01) Lacunas: nenhuma F7.1-D03 - Desativar usuário (PATCH + JTI blacklist + audit\_log)

<!-- image -->

Escopo:  happy  path  -  admin  desativa  um  usuário  ativo;  todos  os  tokens  JWT  do  usuário  são

invalidados via JTI blacklist Atores: Admin, WebApp, IAMController, DeactivateUserUseCase, Postgres

Pré-condições: admin com user.manage\_all; usuário alvo com status=ATIVO

<!-- image -->

Notas: jti\_blacklist  invalida  imediatamente  todas  as  sessões  ativas;  próximas  requisições  do  usuário retornam 401 (RN-06) deactivate rel ausente no \_links da resposta confirma CA-06 (HATEOAS condicional) Nenhum  dado  histórico  é  excluído  -  soft  delete  via  status=INATIVO  (fora  de  escopo:  exclusão permanente) TX atômica: falha no INSERT jti\_blacklist faz rollback do PATCH - usuário permanece ATIVO Lacunas: nenhuma F7.8-D04 - Reset de senha administrativo (POST + JWT 1-uso + outbox) Escopo:  happy  path  -  admin  dispara  reset  de  senha  para  um  usuário;  link  JWT  1-uso  (24  h)  é

enviado por e-mail via Outbox; admin nunca vê a senha

Atores: Admin, WebApp, IAMController, ResetPasswordAdminUseCase, Postgres

Pré-condições: admin com user.reset\_password; usuário alvo existe Notas: Admin nunca visualiza o link nem a senha (RN-07) - modal apenas confirma envio Link  enviado  ao  usuário:  /redefinir-senha?token=&lt;JWT&gt; → fluxo  de  consumo  em  US-F0-003 (F0.3-a) Token expirado (CA-05) → DRY para US-F0-003 F0.3-b - mecanismo idêntico Fase de dispatch (OutboxDispatcher → MailAdapter) → transversal/10.1 audit\_log inclui operadorId, targetUserId, acao='RESET\_PASSWORD', timestamp (RN-09) Lacunas: nenhuma F7.1-ERRO-01 - 403 FGAC: user.manage\_all ausente Escopo: erro - usuário sem capability user.manage\_all tenta acessar /admin/usuarios Atores: Admin (sem permissão), WebApp, IAMController Pré-condições: token JWT válido, mas sem user.manage\_all nas authorities Notas: @PreAuthorize("hasAuthority('user.manage\_all')") no controller - Spring Security rejeita antes de atingir o use case RFC 7807 Problem Details: type: access\_denied, status: 403, title: Acesso negado Mesmo padrão aplica-se ao POST /users/:id/password-reset sem user.reset\_password Lacunas: nenhuma HU 44. US-F7-002 - Perfis (Roles) e Matriz de Autoridades COMO administrador da plataforma QUERO criar  e  gerenciar  perfis  (roles)  como  agregadores  de  capabilities,  definir  capabilities  granulares (authorities) e atribuí-las a usuários via matriz PARA

<!-- image -->

<!-- image -->

o  sistema  de  controle  de  acesso  (FGAC)  reflita  exatamente  as  responsabilidades  de  cada pessoa sem necessidade de mudanças de código.

DESENHO DA(S) TELA(S)

[INSERIR TELA WEB AQUI]

[INSERIR TELA MOBILE AQUI]

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Listar perfis

DADO QUE o admin acessa /admin/perfis QUANDO a tabela carrega ENTÃO são  exibidos  os  perfis  com  Nome,  Descrição,  Número  de  authorities  vinculadas,  Número  de usuários os perfis pré-definidos têm badge "Sistema" e botão "Excluir" oculto

Critério 2: Criar perfil customizado

DADO QUE o admin clica em "Novo perfil" QUANDO preenche Nome "membro\_caaf" e seleciona as authorities: formative.review, formative.assign salva ENTÃO a API recebe POST /admin/perfis com as authorities o perfil aparece na tabela com badge "Customizado"

Critério 3: Excluir perfil com usuários ativos

DADO QUE o perfil "membro\_caaf" está atribuído a 3 usuários ativos QUANDO o admin tenta excluí-lo ENTÃO a API retorna HTTP 422 a UI exibe AlertBanner: "Perfil em uso por 3 usuários - remova antes de excluir"

Critério 4: Listar e editar autoridades com matriz

DADO QUE o admin acessa /admin/autoridades QUANDO a tela carrega ENTÃO a tabela exibe as authorities com Nome, Módulo, Descrição abaixo da tabela o DS/RoleAuthorityMatrix exibe a grade role × authority com checkboxes QUANDO o admin marca "PROFESSOR" × "event.host" salva ENTÃO a API atualiza o perfil PROFESSOR adicionando a authority event.host o cache de capabilities de todos os usuários com perfil PROFESSOR é invalidado

Critério 5: Atribuir role a usuário (modal)

DADO QUE

o admin clica em "Gerenciar roles" na linha do usuário "Maria" QUANDO o modal de matriz de roles abre ENTÃO os checkboxes mostram os perfis atuais de Maria marcados QUANDO o admin adiciona o perfil "membro\_caaf" e remove "SECRETARIA" confirma ENTÃO a API recebe PUT /users/maria/roles com a nova lista de perfis o cache de capabilities de Maria é invalidado o audit\_log registra a alteração Critério 6: Authority somente leitura DADO QUE a authority "request.deliberate" foi definida no código QUANDO o admin tenta renomeá-la ENTÃO o campo Nome está desabilitado apenas o campo Descrição é editável DIAGRAMAS DE SEQUÊNCIA F7.2-D01 - Listar perfis (happy path + HATEOAS) Escopo:  happy  path  -  admin  acessa  /admin/perfis;  API  retorna  página  de  roles  com  \_links condicionais (delete ausente para perfis pré-definidos) Atores: Admin, WebApp, IAMController, ListRolesUseCase, Postgres

Pré-condições: admin autenticado com capability iam.manage\_roles Notas: JwtFilter valida Bearer e verifica iam.manage\_roles antes do controller (inline no passo 2) delete rel ausente para perfis pré-definidos (ALUNO, PROFESSOR, SECRETARIA, COORDENADOR, EGRESSO, ADMIN) - RN-03 Badges  "Sistema"  ou  "Customizado"  derivados do campo type do DTO (pré-definido = SYSTEM, customizado = CUSTOM) Diagrama relacionado: F7.2-D02 (criar), F7.2-ERRO-01 (excluir com usuários) Lacunas: nenhuma F7.2-D02 - Criar perfil customizado (POST + audit\_log)

<!-- image -->

Escopo: happy path - admin cria novo perfil customizado com conjunto inicial de authorities Atores: Admin, WebApp, IAMController, CreateRoleUseCase, Postgres Pré-condições: admin com iam.manage\_roles; nome do perfil inexistente Notas: Nome em snake\_case único (ex.: membro\_caaf, professor\_tcc) - RN-02; 409 Conflict se já existir (verificado antes da TX via SELECT BY nome) TX atômica: perfil + vínculos perfil\_authority + audit\_log no mesmo COMMIT audit\_log registra operadorId, acao, payload (authorityIds incluídos) - RN-13 Lacunas: nenhuma F7.2-D03 - Listar autoridades + carregar DS/RoleAuthorityMatrix Escopo: happy path - admin acessa /admin/autoridades; API retorna authorities e dados da grade role×authority para o componente DS/RoleAuthorityMatrix Atores: Admin, WebApp, IAMController, ListAuthoritiesUseCase, Postgres Pré-condições: admin com iam.manage\_authorities Notas: Authorities com systemDefined=true retornam campo readOnlyName=true → frontend desabilita input Nome (RN-10; CA-06 → NAO\_APLICAVEL - lógica UI pura) PATCH  /admin/autoridades/:id  {descricao}  (editar  descrição)  é  um  PATCH  simples  sem  cache invalidation; segue padrão de F7.2-D04 com escopo menor Matrix data inclui colunas = perfis, linhas = authorities, células = booleano assigned Diagrama relacionado: F7.2-D04 (salvar delta da matriz) Lacunas: nenhuma F7.2-D04 - PATCH matriz role × authority + invalidação de cache Escopo:  happy  path  -  admin  salva  alteração  na  DS/RoleAuthorityMatrix  (adiciona/remove authority de um perfil); cache de capabilities dos usuários afetados é invalidado Atores: Admin, WebApp, IAMController, UpdateRoleAuthoritiesUseCase, Postgres, CapabilityCache Pré-condições: admin com iam.manage\_roles; perfil alvo existe Notas: Abordagem delta (add/remove) minimiza escritas - apenas diferenças, não substituição total do conjunto - RN-09 Cache invalidation afeta todos os usuários com o perfil alterado, não apenas um (RN-12) CapabilityCache pode ser Redis (produção) ou cache local Spring @CacheEvict no MVP audit\_log inclui roleId, delta.add, delta.remove, operadorId, timestamp - RN-13 Lacunas: nenhuma F7.2-D05 - Atribuir roles a usuário via modal (PUT + cache invalidação) Escopo:  happy  path  -  admin  abre  modal  de  roles  a  partir  de  F7.1  (\_link  manage-roles),  edita seleção de perfis do usuário e confirma; cache de capabilities do usuário é invalidado Atores: Admin, WebApp, IAMController, AssignRolesUseCase, Postgres, CapabilityCache Pré-condições: admin com iam.manage\_roles; acesso via \_link manage-roles de US-F7-001 F7.1

<!-- image -->

<!-- image -->

<!-- image -->

<!-- image -->

Notas: PUT substitui o conjunto completo de roles (idempotente) - RN-11 REPLACE user\_roles = DELETE WHERE userId + INSERT novos vínculos dentro da TX Cache invalidation de capabilities imediata após COMMIT - RN-12 audit\_log registra operadorId, targetUserId, roles adicionados e removidos (diff) - RN-13 Diagrama relacionado: F7.2-D01 (origin do \_link manage-roles via F7.1) Lacunas: nenhuma

F7.2-ERRO-01 - 422 Excluir perfil com usuários ativos

Escopo: erro - admin tenta excluir perfil customizado que ainda está atribuído a usuários ativos; API rejeita com 422 Atores: Admin, WebApp, IAMController, DeleteRoleUseCase, Postgres Pré-condições: admin com iam.manage\_roles; perfil alvo é type='CUSTOM' com usuários ativos

<!-- image -->

Notas:

RFC  7807:  type:  role\_in\_use,  status:  422,  detail:  "3  usuários  ativos  com  este  perfil"  -  corpo completo em Notas (não inline na seta) Perfis pré-definidos (type='SYSTEM') nem chegam ao use case - botão "Excluir" ausente via \_links (F7.2-D01) Fluxo de sucesso (0 usuários bloqueantes): TX DELETE perfil + DELETE role\_authority + INSERT audit\_log + COMMIT

Lacunas: nenhuma

F7.2-ERRO-02 - 403 FGAC: iam.manage\_roles / iam.manage\_authorities ausente

Escopo: erro - usuário sem capability IAM tenta acessar /admin/perfis ou /admin/autoridades Atores: Admin (sem permissão), WebApp, IAMController Pré-condições: token JWT válido, mas sem iam.manage\_roles ou iam.manage\_authorities

<!-- image -->

Notas: @PreAuthorize("hasAuthority('iam.manage\_roles')")  no  controller  - Spring Security rejeita antes do use case DRY → F7.1-ERRO-01 - padrão idêntico; capability diferente (iam.manage\_roles vs user.manage\_all) /admin/autoridades usa iam.manage\_authorities; mesmo diagrama com capability substituída

## HU 45. US-F7-003 - Editor de Tipos de Solicitação (Workflow Engine)

COMO administrador da plataforma QUERO (state machine JSON) através de um editor visual de 3 painéis com preview ao vivo PARA

criar e editar tipos de solicitação definindo seu formulário (JSON Schema) e seu fluxo de trabalho novos processos acadêmicos sejam suportados pelo sistema apenas com a configuração de dados, sem necessidade de novas deployments. DESENHO DA(S) TELA(S) [INSERIR TELA WEB AQUI] [INSERIR TELA MOBILE AQUI] FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Exibição dos três painéis DADO QUE o admin acessa /admin/tipos-solicitacao QUANDO

a tela carrega ENTÃO o painel esquerdo exibe a lista de RequestTypes com nome e status (DRAFT/PUBLISHED) o painel central exibe os editores JSON (form\_schema e workflow\_json) do tipo selecionado o painel direito exibe o preview do formulário e o grafo do workflow

Critério 2: Edição de form\_schema com preview ao vivo

DADO QUE o admin seleciona o tipo "Trancamento" QUANDO ele adiciona um novo campo "motivoTrancamento" ao JSON Schema ENTÃO o DS/FormSchemaPreview atualiza imediatamente exibindo o novo campo o campo aparece como textarea no preview

Critério 3: Schema inválido bloqueia publicação

DADO QUE o admin edita o form\_schema com JSON malformado (chave sem fechar) QUANDO o editor detecta o erro de sintaxe ENTÃO o DS/JsonSchemaEditor exibe borda status/danger uma mensagem de erro "JSON inválido na linha N" aparece abaixo do editor o botão "Publicar" está desabilitado

DADO QUE o admin finalizou a edição do form\_schema e workflow\_json de "Colação grau" ambos estão válidos

Critério 4: Publicar versão QUANDO

ele clica em "Publicar" ENTÃO a API recebe POST /request-types/:id/publish uma nova versão imutável é criada com número de versão incrementado o status muda para PUBLISHED novas solicitações deste tipo usam a versão corrente solicitações já abertas mantêm a versão anterior

Critério 5: Versionamento: solicitações existentes não afetadas

DADO QUE existem 5 solicitações abertas do tipo "Aproveitamento" usando a versão 2 QUANDO o admin publica a versão 3 com um novo campo obrigatório ENTÃO as 5 solicitações existentes continuam sendo processadas com o formulário da versão 2 apenas novas solicitações de "Aproveitamento" exibem o campo novo

Critério 6: Workflow grafo atualiza com o JSON

DADO QUE o admin edita o workflow\_json adicionando um novo estado "EM\_RECURSO" QUANDO o JSON é salvo em memória (sem publicar) ENTÃO o DS/WorkflowStateMachineEditor exibe o novo nó "EM\_RECURSO" no grafo as transições do estado são renderizadas como edges

Critério 7: Criar novo tipo de solicitação

DADO QUE o admin clica em "Novo" QUANDO

preenche o Nome "Monitoria" e define form\_schema e workflow\_json válidos clica em "Publicar" ENTÃO o RequestType "Monitoria" aparece no wizard de nova solicitação (F1.8/F5.3) é adicionado ao painel esquerdo com status PUBLISHED

DIAGRAMAS DE SEQUÊNCIA

F7.4-D01 - Listar tipos e carregar editor de três painéis

Escopo:  happy  path  -  admin  acessa  /admin/tipos-solicitacao;  lista  é  carregada  e  o  tipo selecionado popula os painéis central e direito Atores: Admin, WebApp, RTController, ListRTUseCase, GetRTUseCase, Postgres Pré-condições: admin com request\_type.manage

<!-- image -->

Notas: Painel esquerdo: lista com badges DRAFT / PUBLISHED e contagem dos 19 tipos (RN-11) Painel  central:  dois  DS/JsonSchemaEditor com os conteúdos form\_schema e workflow\_json do tipo selecionado Painel direito: DS/FormSchemaPreview + DS/WorkflowStateMachineEditor populados client-side a partir da resposta 200 \_links inclui: publish (se DRAFT + válido), save-draft, delete (se DRAFT sem histórico) Lacunas: nenhuma F7.4-D02 - Criar novo RequestType (POST → status DRAFT)

Escopo:  happy  path  -  admin  cria  novo  tipo  "Monitoria"  com  schemas  iniciais;  sistema  persiste como DRAFT Atores: Admin, WebApp, RTController, CreateRTUseCase, Postgres Pré-condições: admin com request\_type.manage

<!-- image -->

Notas: Schema inválido na criação → self-call  retorna  422  antes  da  TX (ver F7.4-ERRO-02 para publish; mesma lógica) audit\_log registra payload completo de form\_schema e workflow\_json (RN-10) Tipo DRAFT não aparece no wizard de nova solicitação (F1.8/F5.3) - RN-08 Diagrama relacionado: F7.4-D04 (publicar após edição) Lacunas: nenhuma

F7.4-D03 - Salvar rascunho (PATCH form\_schema + workflow\_json)

Escopo: happy path - admin edita schemas de um tipo DRAFT e persiste o rascunho sem publicar Atores: Admin, WebApp, RTController, SaveDraftUseCase, Postgres Pré-condições: tipo alvo em status='DRAFT'; admin com request\_type.manage

<!-- image -->

Notas: PATCH só possível enquanto status='DRAFT' - tipos PUBLISHED não são editados in-place (nova publicação cria versão) audit\_log inclui payload completo dos schemas para rastreabilidade de versionamento (RN-10) validate() no UseCase: schema inválido → 422 antes da TX (F7.4-ERRO-02) Após COMMIT, client-side atualiza DS/FormSchemaPreview e DS/WorkflowStateMachineEditor a partir da resposta 200 Lacunas: nenhuma F7.4-D04 - Publicar versão (POST /publish + versionamento atômico)

Escopo:  happy  path  -  admin  publica  um  RequestType  DRAFT;  nova  versão  imutável  é  criada; solicitações existentes mantêm a versão anterior Atores: Admin, WebApp, RTController, PublishRequestTypeUseCase, Postgres Pré-condições:  tipo  em  status='DRAFT';  form\_schema  e  workflow\_json  válidos;  admin  com request\_type.manage

<!-- image -->

Notas:

request\_type\_version  é  imutável  -  snapshot  completo  de  form\_schema  +  workflow\_json  na versão N+1 (RN-07) Solicitações  abertas  mantêm  request\_type\_version\_id  = N (CA-05 → DRY  -  FK  preservada  na criação da solicitação) A partir deste COMMIT, o wizard de nova solicitação (F1.8/F5.3) resolve currentVersion = N+1 Schema inválido no momento do publish → 422 antes da TX (F7.4-ERRO-02) Lacunas: nenhuma

F7.4-ERRO-01 - 403 FGAC: request\_type.manage ausente

Escopo: erro - usuário sem request\_type.manage tenta acessar /admin/tipos-solicitacao Atores: Admin (sem permissão), WebApp, RTController Pré-condições: token JWT válido; sem request\_type.manage nas authorities

<!-- image -->

Notas: @PreAuthorize("hasAuthority('request\_type.manage')") - Spring Security rejeita antes do use case DRY → F7.1-ERRO-01 - padrão idêntico (@PreAuthorize + RFC 7807 403)

Aplica-se a todos os endpoints desta HU (GET, POST, PATCH, DELETE, POST /publish) Lacunas: nenhuma

F7.4-ERRO-02 - 422 Schema inválido no publish (server-side)

Escopo:  erro  -  admin  tenta  publicar  RequestType  com  form\_schema  malformado;  API  rejeita antes da TX Atores: Admin, WebApp, RTController, PublishRequestTypeUseCase, Postgres Pré-condições: tipo em status='DRAFT'; form\_schema com JSON inválido (ex.: chave sem fechar)

<!-- image -->

Notas: RFC 7807: type: invalid\_schema, status: 422, detail: "form\_schema: SyntaxError at line N" - corpo completo em Notas Mesma lógica aplica-se ao workflow\_json inválido (tipo: invalid\_workflow) Validação client-side (Monaco) é best-effort - server-side é a barreira definitiva (RN-03) Nenhuma TX é iniciada antes do validate - sem efeito colateral Lacunas: nenhuma F7.4-ERRO-03 - 422 Excluir RequestType com histórico

Escopo: erro - admin tenta excluir um RequestType que possui solicitações ou versões históricas; API rejeita com 422

Atores: Admin, WebApp, RTController, DeleteRequestTypeUseCase, Postgres

Pré-condições: admin com request\_type.manage; tipo alvo com solicitações ou versões existentes

<!-- image -->

Notas:

RFC  7807:  type:  request\_type\_in\_use,  status:  422,  detail:  "5  solicitações  vinculadas"  -  corpo completo em Notas

Apenas tipos em status='DRAFT' sem nenhuma request\_type\_version e sem solicitações podem ser excluídos (RN-09)

\_links omite delete para tipos PUBLISHED → botão ausente via HATEOAS (capturado em F7.4-D01) Padrão idêntico a F7.2-ERRO-01 (excluir perfil com usuários ativos)

Lacunas: nenhuma

## HU 46. US-F7-004 - Templates de Comunicação

COMO

administrador da plataforma

QUERO

criar  e  editar  templates  de  e-mail  e  notificação  em  Markdown  com  placeholders  dinâmicos  e visualizar o preview com variáveis substituídas

PARA

as  comunicações automáticas do sistema (deliberações, notificações, boas-vindas) tenham texto correto e possam ser ajustados sem alteração de código.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Exibição do editor dividido DADO QUE o admin acessa /admin/templates-comunicacao seleciona o template "aproveitamento.deferido" QUANDO

a tela carrega ENTÃO o DS/MarkdownEditor exibe o conteúdo Markdown do template

o DS/TemplatePreview renderiza o HTML com variáveis substituídas por exemplos a coluna direita lista o histórico de versões com status CURRENT e ARCHIVED Critério 2: Preview ao vivo com placeholders DADO QUE o corpo do template contém "Olá {{nome}}, sua solicitação {{protocolo}} foi deferida." QUANDO o preview renderiza ENTÃO exibe "Olá João da Silva, sua solicitação SOL-2026-001 foi deferida." qualquer alteração no editor atualiza o preview imediatamente Critério 3: Placeholder inválido destacado DADO QUE o admin digita "{{aluno\_email}}" (variável não disponível) QUANDO o preview renderiza ENTÃO o placeholder aparece destacado em vermelho (status/danger) um tooltip exibe "Variável não reconhecida - use: nome, protocolo, curso, link" Critério 4: Salvar cria nova versão o admin editou o corpo do template

DADO QUE QUANDO clica em "Salvar" ENTÃO

a API cria uma nova revisão com número incrementado a revisão anterior muda para status ARCHIVED

- a nova revisão aparece no histórico com status CURRENT e timestamp atual

Critério 5: Visualizar versão anterior (somente leitura)

DADO QUE existem 3 versões do template "boas-vindas.egresso" QUANDO o admin clica na versão 1 no histórico ENTÃO o editor exibe o conteúdo da versão 1 um banner informativo exibe "Versão 1 - somente leitura" o botão "Salvar" está desabilitado

Critério 6: Autocomplete de placeholders

DADO QUE o admin está editando o corpo no DS/MarkdownEditor QUANDO ele digita "{{" ENTÃO uma lista de sugestões de placeholders aparece: nome, protocolo, curso, link, data QUANDO ele seleciona "protocolo" ENTÃO "{{protocolo}}" é inserido no cursor

DIAGRAMAS DE SEQUÊNCIA

F7.5-D01 - Listar templates e carregar editor (two-column)

Escopo:  happy  path  -  admin  acessa  /admin/templates-comunicacao,  seleciona  um  template; editor de duas colunas é carregado com conteúdo, variáveis e histórico de versões Atores: Admin, WebApp, CTController, ListTemplatesUseCase, GetTemplateUseCase, Postgres Pré-condições: admin com communication.manage\_templates

<!-- image -->

Notas: variaveis[] na resposta alimenta client-side: autocomplete de {{ (CA-06) e validação de placeholders (CA-03) - sem chamadas adicionais Histórico revisoes[] inclui {versao, autor, criadoEm, status} para a coluna direita DS/DataTable/Full JwtFilter  valida  Bearer  e  verifica  communication.manage\_templates  antes  do  controller  (inline passo 2) Diagrama relacionado: F7.5-D03 (salvar revisão), F7.5-D04 (carregar versão anterior) Lacunas: nenhuma

F7.5-D02 - Criar novo template (POST)

Escopo: happy path - admin cria novo template de comunicação com primeira revisão Atores: Admin, WebApp, CTController, CreateTemplateUseCase, Postgres Pré-condições: admin com communication.manage\_templates; nome do template inexistente Notas: Nome em dot-notation (ex.: boas-vindas.egresso) - único; 409 Conflict se já existir (verificado via SELECT BY nome antes da TX) TX atômica: cabeçalho do template + primeira revisão + audit\_log no mesmo COMMIT variaveis lista os placeholders disponíveis (ex.: [nome, protocolo, curso, link]) - RN-02 audit\_log registra operadorId, acao='CREATE\_TEMPLATE', payload (RN-10) Lacunas: nenhuma F7.5-D03 - Salvar revisão (POST /revisions + versionamento imutável) Escopo: happy path - admin edita corpo/assunto e salva; nova revisão é criada como CURRENT; revisão anterior arquivada atomicamente Atores: Admin, WebApp, CTController, SaveRevisionUseCase, Postgres Pré-condições: template existente; admin com communication.manage\_templates Notas: Revisões  anteriores  nunca  são  excluídas  -  apenas  status='ARCHIVED'  (RN-06  - auditabilidade imutável)

<!-- image -->

<!-- image -->

TX  atômica  garante  que  não  pode  haver  dois  CURRENT  simultâneos  - UPDATE → INSERT → COMMIT Coluna direita DS/DataTable/Full atualiza a partir da resposta 200 (novo item no topo com status CURRENT) audit\_log inclui versao=N+1, operadorId, templateId, payload resumido (RN-10) Lacunas: nenhuma

F7.5-D04 - Carregar versão anterior (somente leitura)

Escopo: happy path - admin clica em revisão arquivada no histórico; editor carrega conteúdo da versão em modo somente leitura Atores: Admin, WebApp, CTController, GetRevisionUseCase, Postgres Pré-condições: template com ≥ 2 revisões; admin com communication.manage\_templates

<!-- image -->

Notas: Modo readonly é lógica UI baseada em status='ARCHIVED' na resposta - sem endpoint adicional (CA-05) Botão "Salvar" desabilitado pelo frontend; não há guard backend para leitura de versão arquivada O preview DS/TemplatePreview ainda renderiza o conteúdo da versão histórica client-side Para retornar à versão corrente: admin clica na revisão CURRENT no histórico (mesmo endpoint, versions/N)

F7.5-ERRO-01 - 403 FGAC: communication.manage\_templates ausente

Escopo: erro - usuário sem communication.manage\_templates tenta acessar /admin/templates-comunicacao Atores: Admin (sem permissão), WebApp, CTController Pré-condições: token JWT válido; sem communication.manage\_templates nas authorities Notas: @PreAuthorize("hasAuthority('communication.manage\_templates')") - Spring Security rejeita antes do use case DRY → F7.1-ERRO-01 - padrão idêntico; capability diferente Aplica-se  a  todos  os  endpoints  desta  HU  (GET,  POST,  POST  /revisions,  GET  /versions,  GET /versions/:rev) Lacunas: nenhuma

<!-- image -->

## HU 47. US-F7-005 - Observabilidade do Outbox e Jobs Agendados

COMO administrador da plataforma QUERO

monitorar o estado dos eventos do Outbox (PENDING, SENT, FAILED, DEAD) e dos jobs agendados, com a capacidade de reenviar eventos falhos

PARA

eu possa diagnosticar e corrigir falhas na entrega de e-mails e notificações sem precisar de acesso direto ao banco de dados.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Visualizar eventos por status DADO QUE o admin acessa /admin/jobs QUANDO a aba FAILED está ativa ENTÃO o DS/OutboxEventTable exibe apenas eventos com status FAILED cada linha mostra ID, Aggregate Type, Payload resumido, Tentativas, Criado em as linhas FAILED têm badge status/danger Critério 2: Filtrar por aggregate type DADO QUE o admin seleciona o filtro "Aggregate Type: solicitacoes" QUANDO o filtro é aplicado ENTÃO a tabela exibe somente eventos com aggregate\_type = "solicitacoes"

Critério 3: Reentregar evento DEAD

DADO QUE existe um evento com status DEAD (5 tentativas esgotadas) QUANDO o admin clica em "Reentregar" na linha do evento ENTÃO a API recebe POST /admin/outbox/:id/retry o evento muda para status PENDING o badge atualiza imediatamente na tabela

um toast "Evento reenfileirado" é exibido

Critério 4: Alerta de latência do dispatcher DADO QUE o Outbox tem eventos PENDING há mais de 30 s sem processamento QUANDO o admin visualiza a tela ENTÃO um DS/AlertBanner de aviso aparece: "Dispatcher com latência &gt; 30s verificar OutboxDispatcher" Critério 5: Scheduled Jobs DADO QUE a seção de Scheduled Jobs é exibida QUANDO o admin visualiza os DS/ScheduledJobCard ENTÃO cada card exibe: nome do job, frequência, último run, próximo run, status o job "EventAutoCloser" com status ATRASADO exibe badge status/warning Critério 6: Botão Reentregar somente para FAILED/DEAD DADO QUE um evento tem status SENT QUANDO o admin visualiza a linha ENTÃO o botão "Reentregar" não é exibido (rel "retry" ausente via HATEOAS) DIAGRAMAS DE SEQUÊNCIA F7.6-D01 - Listar outbox events por status (happy path + HATEOAS) Escopo:  happy  path  -  admin  acessa  /admin/jobs  com  aba  FAILED  ativa;  API  retorna  eventos paginados com \_links condicionais e metadados de latência do dispatcher Atores: Admin, WebApp, OutboxController, ListOutboxEventsUseCase, Postgres Pré-condições: admin com system.observe Notas: \_links.retry  presente  apenas  para  status=FAILED  e  status=DEAD (RN-06); ausente para SENT e PENDING - CA-06 → DRY (capturado aqui) meta.oldestPendingMs  =  now  -  min(created\_at)  WHERE  status=PENDING;  se  &gt;  30  000 → DS/AlertBanner exibido client-side (CA-04 → DRY) Filtro por aggregateType é adição de query param no mesmo endpoint - CA-02 → DRY Paginação  20  eventos/página  (RN-10);  eventos  SENT  retidos  7  dias  antes  de  arquivamento automático Lacunas: nenhuma F7.6-D02 - Reentregar evento DEAD/FAILED → PENDING

<!-- image -->

Escopo:  happy  path  -  admin  clica  "Reentregar"  em  evento  DEAD;  API  recoloca  o  evento  em PENDING para o próximo ciclo do OutboxDispatcher

Atores: Admin, WebApp, OutboxController, RetryOutboxEventUseCase, Postgres

Pré-condições: evento com status=DEAD ou status=FAILED; admin com system.observe Notas: SELECT FOR UPDATE previne race condition em cliques simultâneos de dois admins (RN-05) tentativas=0 reinicia o contador - evento DEAD voltará a ter 5 tentativas disponíveis OutboxDispatcher processa o evento no próximo ciclo de 5 s → transversal/10.1b retried\_by registra o operador na linha do outbox\_event sem necessidade de audit\_log separado (a tabela outbox é rastreável) Botão "Reentregar" some do frontend porque a resposta 200 traz \_links sem retry (status=PENDING) Lacunas: nenhuma F7.6-D03 - Listar jobs agendados (scheduled jobs dashboard)

<!-- image -->

Escopo: happy path - admin visualiza a seção de Scheduled Jobs; API retorna status atual de cada

job recorrente Atores: Admin, WebApp, JobsController, ListScheduledJobsUseCase, Postgres Pré-condições: admin com system.observe

<!-- image -->

Notas: Jobs documentados (RN-09): OutboxDispatcher (5 s) · SlaBreachChecker (diário) · ExportJobCleaner (diário) · EventAutoCloser (23:59 diário) status  calculado  no  backend:  OK  se  ultimoRun  recente  +  sem erro; ATRASADO se proximoRun &lt; now; FALHOU se último run com exceção Leitura de observabilidade pura - sem mutações nem audit\_log Diagrama relacionado: F7.6-D01 (outbox events), US-F7-007 (saúde do sistema - Prometheus/Grafana) Lacunas: nenhuma F7.6-ERRO-01 - 403 FGAC: system.observe ausente Escopo: erro - usuário sem system.observe tenta acessar /admin/jobs Atores: Admin (sem permissão), WebApp, OutboxController Pré-condições: token JWT válido; sem system.observe nas authorities Notas: @PreAuthorize("hasAuthority('system.observe')") - Spring Security rejeita antes do use case DRY → F7.1-ERRO-01 - padrão idêntico; capability system.observe Aplica-se a todos os endpoints desta HU (GET /admin/outbox, POST /retry, GET /admin/scheduled-jobs) Lacunas: nenhuma

<!-- image -->

## HU 48. US-F7-006 - Trilha de Auditoria

COMO administrador da plataforma QUERO pesquisar  a  trilha  de  auditoria  por  ator,  entidade  e  intervalo  de  tempo,  e  visualizar  o  diff  JSON antes/depois de qualquer alteração em um Drawer lateral PARA seja possível investigar incidentes, rastrear mudanças não autorizadas e comprovar a conformidade com políticas de segurança.

DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Listar eventos de auditoria

DADO QUE o admin acessa /admin/audit-log QUANDO a tabela carrega (sem filtros aplicados) ENTÃO são exibidos os eventos do último ano ordenados por timestamp DESC cada linha mostra: Ator, Ação, Entidade Alvo, Timestamp, IP nenhum botão de ação aparece no header da página

Critério 2: Filtrar por ator e ação

DADO QUE o admin preenche o filtro Ator com "João" e seleciona Ação "USER\_DEACTIVATED" QUANDO aplica os filtros ENTÃO a tabela exibe apenas eventos em que João realizou a ação USER\_DEACTIVATED

Critério 3: Abrir Drawer com diff

DADO QUE existe um evento "REQUEST\_DELIBERATED" na tabela QUANDO o admin clica na linha ENTÃO o DS/Drawer abre à direita com 420px de largura o DS/AuditDiffViewer exibe o diff JSON side-by-side campos adicionados aparecem em verde, removidos em vermelho, alterados em amarelo o Drawer captura o foco (focus trap)

Critério 4: Fechar Drawer

DADO QUE o Drawer está aberto QUANDO o admin pressiona Esc ENTÃO o Drawer fecha e o foco retorna à linha da tabela

Critério 5: Imutabilidade

DADO QUE qualquer evento de auditoria existe na tabela ENTÃO não há botão "Excluir" ou "Editar" em nenhuma linha a API não expõe endpoints DELETE ou PATCH para /audit-log

Critério 6: Busca por intervalo estendido

DADO QUE o admin define o intervalo "De: 2021-01-01 Até: hoje" QUANDO aplica os filtros ENTÃO a tabela exibe eventos dos últimos 5 anos o sistema retorna até 50 registros por página dentro desse intervalo

DIAGRAMAS DE SEQUÊNCIA

F7.7-D01 - Listar eventos de auditoria (happy path + filtros)

Escopo: happy path - admin acessa /admin/audit-log; API retorna página de eventos do último ano com payload completo (antes/depois) para uso no Drawer client-side Atores: Admin, WebApp, AuditController, ListAuditLogUseCase, Postgres Pré-condições: admin com audit.read

<!-- image -->

Notas: payloadAntes e payloadDepois incluídos na resposta da lista - abertura do Drawer (CA-03) e diff rendering (RN-05..06) são client-side sem roundtrip adicional ( → NAO\_APLICAVEL) Sem \_links de ação por item - imutabilidade garantida pelo DTO (CA-05, RN-08) Filtros CA-02 (ator + ação) → DRY: mesmo endpoint com &amp;ator=João&amp;acao=USER\_DEACTIVATED Intervalo estendido CA-06 (até 5 anos) → DRY: mesmo GET com &amp;de=2021-01-01 explícito; default = último ano (RN-09) Paginação 50/página, ORDER BY timestamp DESC (RN-10) Lacunas: nenhuma F7.7-ERRO-01 - 403 FGAC: audit.read ausente Escopo: erro - usuário sem audit.read tenta acessar /admin/audit-log Atores: Admin (sem permissão), WebApp, AuditController Pré-condições: token JWT válido; sem audit.read nas authorities Notas: @PreAuthorize("hasAuthority('audit.read')") - Spring Security rejeita antes do use case DRY → F7.1-ERRO-01 - padrão idêntico; capability audit.read audit.read é uma capability restrita: geralmente atribuída apenas ao perfil ADMIN (RN-01) Lacunas: nenhuma

<!-- image -->

## HU 49. US-F7-007 - Saúde do Sistema

COMO administrador da plataforma QUERO visualizar  KPIs  operacionais  em  tempo  real  (latência  P95  da  API,  eventos  pendentes  no  Outbox, erros 5xx e uptime) e acessar diretamente o Grafana para análise aprofundada PARA

eu  possa  detectar  rapidamente  degradações  de  performance  ou  falhas  críticas  sem  precisar monitorar logs manualmente.

## DESENHO DA(S) TELA(S)

<!-- image -->

FONTE: Elaborado pelos autores (2026).

CRITÉRIOS DE ACEITAÇÃO

Critério de contexto:

Critério 1: Exibição dos KPIs

DADO QUE o admin acessa /admin/sistema/saude

QUANDO a tela carrega ENTÃO os 4 KpiCards exibem: API P95 (ms), Outbox pending, 5xx última hora, Uptime (%)

os valores refletem o estado atual via Actuator Critério 2: KPI fora do alvo DADO QUE a latência P95 da API está em 420 ms (acima do alvo de 300 ms) QUANDO os KPIs renderizam ENTÃO o KpiCard de API P95 exibe o valor em status/danger (cor vermelha) Critério 3: Polling automático DADO QUE o admin está visualizando a tela há 35 s QUANDO o polling dispara ENTÃO os valores de todos os KpiCards são atualizados silenciosamente o leitor de tela anuncia a atualização via aria-live="polite" Critério 4: Link para Grafana DADO QUE GRAFANA\_URL está configurado como "https:/ /grafana.ufpr.br" QUANDO o admin clica em " → Abrir Grafana" ENTÃO o navegador abre "https:/ /grafana.ufpr.br" em nova aba Critério 5: Capability restrita DADO QUE um usuário com capability system.observe (mas não system.admin) tenta acessar a rota QUANDO a API é consultada ENTÃO retorna HTTP 403 a UI exibe AlertBanner "Permissão insuficiente"

DIAGRAMAS DE SEQUÊNCIA

F7.9-D01 - Carregar KPIs de saúde do sistema (happy path)

Escopo: happy path - admin acessa /admin/sistema/saude; endpoint agrega 4 métricas do Spring

Boot Actuator em paralelo e retorna summary DTO com indicadores SLA Atores: Admin, WebApp, HealthController, SystemHealthUseCase, ActuatorService Pré-condições: admin com system.admin; Spring Boot Actuator habilitado internamente

<!-- image -->

Notas: ActuatorService chama 4 endpoints em paralelo (internamente ao backend): /actuator/metrics/http.server.requests?tag=quantile:0.95, /actuator/metrics/outbox.pending, /actuator/metrics/http.server.requests?tag=status:5xx&amp;window=1h, /actuator/health (RN-02) Actuator  não  é  exposto  diretamente  ao  browser  -  HealthController  age  como  proxy  interno; endpoint /admin/sistema/saude exige system.admin slaViolations[]  na  resposta lista quais KPIs excedem o alvo (ex.: [{kpi:"p95", value:420, target:300}]) - frontend usa isso para aplicar status/danger (CA-02 → NAO\_APLICAVEL como sequência) Polling CA-03 → DRY: mesmo GET acionado por setInterval(30\_000) no frontend; aria-live="polite" atualiza leitores de tela Link Grafana (CA-04) → URL de GRAFANA\_URL  env  var,  retornada opcionalmente  em meta.grafanaUrl na resposta ou configurada em build-time Lacunas: nenhuma F7.9-ERRO-01 - 403 FGAC: system.admin ausente Escopo: erro - usuário com system.observe (mas sem system.admin) tenta /admin/sistema/saude Atores: Admin (sem permissão), WebApp, HealthController Pré-condições: token JWT válido; tem system.observe mas não system.admin acessar Notas:

<!-- image -->

@PreAuthorize("hasAuthority('system.admin')") - system.observe não é suficiente (CA-05) DRY → F7.1-ERRO-01 - padrão idêntico; capability mais restrita que system.observe system.admin ⊃ system.observe: quem tem system.admin também pode acessar F7.6 (jobs) e F7.7 (audit-log); quem tem apenas system.observe não acessa esta tela Lacunas: nenhuma

- 8.9 CROSS CUTTING

## HU 50. US-F8-001 - Busca Global (Command Palette)

COMO qualquer usuário autenticado QUERO abrir  uma  paleta  de  busca  com  Ctrl+K  / ⌘ K  e  digitar  um  termo  para  encontrar  alunos, solicitações, eventos ou usuários de forma rápida PARA eu possa navegar diretamente ao item  desejado  sem  precisar  percorrer  menus  e  filtros manualmente. DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

DADO QUE o usuário está em qualquer tela autenticada do sistema QUANDO pressiona Ctrl+K (ou ⌘ K no macOS) ENTÃO o DS/CommandPalette é exibido sobre a tela atual com scrim

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Abrir com atalho de teclado o input de busca recebe foco automaticamente o estado Empty é exibido Critério 2: Busca com debounce e loading DADO QUE o usuário digitou "joão" no input de busca QUANDO passam 200 ms sem nova tecla ENTÃO a API recebe GET /search?q=joão o estado Loading é exibido com skeleton rows durante a requisição Critério 3: Exibição de resultados agrupados por tipo DADO QUE a API retornou: 3 alunos, 2 solicitações, 1 evento para "joão" QUANDO os resultados são renderizados ENTÃO aparecem 3 seções: "Alunos" (3 itens), "Solicitações" (2 itens), "Eventos" (1 item) cada item exibe título, subtítulo e ícone do tipo Critério 4: Escopo por capability o índice "users" não retorna outros usuários (aluno sem capability user.manage\_all)

DADO QUE um aluno faz a busca "João" QUANDO a API processa a requisição ENTÃO o índice "students" retorna apenas o próprio perfil do aluno (se houver match) o índice "requests" retorna apenas as solicitações do próprio aluno Critério 5: Navegação por teclado e abertura de item DADO QUE os resultados estão exibidos QUANDO o usuário pressiona ↓ duas vezes para selecionar o segundo resultado pressiona Enter ENTÃO a paleta fecha o usuário é redirecionado para a tela de detalhe do item selecionado Critério 6: Fechar com Esc DADO QUE a paleta está aberta com resultados QUANDO o usuário pressiona Esc ENTÃO a paleta fecha sem navegar o foco retorna ao elemento que estava ativo antes da abertura Critério 7: Mobile: tela cheia DADO QUE o usuário está em um dispositivo móvel (375px) QUANDO clica no input de busca na topbar ENTÃO a busca abre em tela cheia (não modal) com o DS/Input/Search expandido o botão "Cancelar" aparece ao lado do input QUANDO clica em "Cancelar" ENTÃO

retorna à tela anterior sem navegar Critério 8: Sem resultados encontrados DADO QUE o usuário digitou "xyzxyz123" e não há resultados QUANDO a API retorna array vazio ENTÃO o estado Empty é exibido com texto "Nenhum resultado encontrado para 'xyzxyz123'" DIAGRAMAS DE SEQUÊNCIA F8.1-D01 - Busca com debounce + fan-out paralelo + resultados agrupados (happy path) Escopo: happy path - usuário digita ≥ 2 chars; API retorna resultados em pelo menos um índice Atores: Usuário autenticado (qualquer perfil), WebApp Pré-condições: JWT válido; pg\_trgm habilitado em students, requests, events, users Notas: Passo 5-12: as 4 queries são executadas em paralelo dentro de SearchUseCase; cada uma aplica sua própria cláusula de capability antes de rodar (ver D02 para detalhe FGAC). O índice users só é consultado se o token carregar user.manage\_all; caso contrário retorna [] sem tocar a tabela. pg\_trgm  (GIN)  em  students.nome,  events.titulo  viabiliza  ILIKE  eficiente;  requests  busca  por número de protocolo e tipo.

<!-- image -->

F8.1-D02 - Fan-out FGAC: escopo por capability (perfil Aluno)

Escopo: mesma query GET /search, mas com token de Aluno - mostra como o SearchUseCase filtra cada índice pelas capabilities do token Atores: WebApp (Aluno autenticado) Pré-condições: JWT com {student.view\_own, request.view\_own, event.view} - sem user.manage\_all Notas: Passo 3 (self-call): SearchUseCase verifica capabilities.contains("user.manage\_all") antes de montar o plano de queries - não precisa tocar o banco para saber que o índice users deve ser omitido. student.view\_own restringe a WHERE com grr = :userGrr - o aluno nunca vê registros de outros alunos, mesmo que o nome coincida. request.view\_own  restringe  a  WHERE  com  student\_id  =  :userId  -  o  aluno  só  encontra  suas próprias solicitações. O resultado usuarios: [] não é erro - é comportamento esperado por design FGAC (RN-02, RN-11).

<!-- image -->

F8.1-D03 - Sem resultados encontrados (empty state pós-API) Escopo:  query  válida  (≥  2  chars),  porém  nenhum  índice  retorna  correspondência → estado EmptyState Atores: Usuário autenticado, WebApp Pré-condições: JWT válido; termo buscado sem correspondência em nenhuma tabela

<!-- image -->

Notas: O  fan-out  (passo  5)  segue  o  mesmo  padrão  de  D01  (4  queries  paralelas  com  capability  filter);  a diferença é exclusivamente no retorno do banco. O status HTTP continua 200 (não 404) - ausência de resultados é uma resposta válida do endpoint de busca. RN-09 distingue dois estados Empty: (a) query &lt; 2 chars - sem chamada de API

(NAO\_APLICAVEL); (b) query ≥ 2 chars com 200 vazio - este diagrama.

F8.1-D04 - Timeout 5s no cliente (AbortController)

Escopo: erro de rede ou lentidão extrema - cliente cancela a requisição após 5s sem resposta

Atores: Usuário autenticado, WebApp

Pré-condições: JWT válido; rede instável ou backend sobrecarregado (&gt; 5s de latência)

<!-- image -->

Notas: O timeout de 5s é responsabilidade do cliente (RN-10): AbortController com signal.timeout(5000) cancelando  o  fetch.  O  servidor  pode  continuar  processando  após  o  abort,  mas  a  resposta  é descartada. SearchController não aparece na resposta (passo 3) porque a conexão TCP é abortada pelo cliente antes de qualquer retorno - não há response body a processar. A mensagem de erro exibida pelo WebApp deve orientar o usuário a tentar novamente (não é erro

4xx/5xx do servidor).

## HU 51. US-F8-002 - Suporte e FAQ

COMO qualquer usuário autenticado

## QUERO

consultar uma base de perguntas frequentes (FAQ) e, caso não encontre a resposta, abrir um ticket de suporte diretamente pela plataforma

## PARA

eu possa resolver dúvidas operacionais sem precisar contatar a secretaria por e-mail ou telefone.

## DESENHO DA(S) TELA(S)

<!-- image -->

<!-- image -->

FONTE: Elaborado pelos autores (2026). CRITÉRIOS DE ACEITAÇÃO Critério de contexto: Critério 1: Exibir FAQ com Accordion DADO QUE o usuário acessa /suporte QUANDO a página carrega ENTÃO a seção FAQ exibe as perguntas como DS/Accordion recolhidos o item "Como redefinir minha senha?" está expandido por padrão mostrando a resposta QUANDO o usuário clica em "Prazo de deliberação?" ENTÃO o painel de resposta é expandido e o ícone de chevron gira Critério 2: Navegação por teclado no FAQ

DADO QUE o usuário navega pelo FAQ via Tab QUANDO pressiona Tab para focar na pergunta "Prazo de deliberação?" pressiona Enter ENTÃO o item é expandido o attr aria-expanded muda para "true"

Critério 3: Enviar ticket com sucesso

DADO QUE o usuário preenche Assunto "Dúvida sobre prazo de aproveitamento" e Mensagem "..." QUANDO clica em "Enviar ticket" ENTÃO a API recebe POST /support/tickets o estado Submit é exibido com DS/AlertBanner success o banner inclui o número do protocolo gerado (ex.: "SUP-2026-042") o formulário é limpo mas permanece visível

Critério 4: Campos obrigatórios

DADO QUE o usuário deixou o campo Assunto em branco QUANDO

clica em "Enviar ticket" ENTÃO

o formulário exibe erro inline "Assunto é obrigatório" a API não é chamada Critério 5: Rate limit de tickets DADO QUE o usuário já enviou 3 tickets na última hora QUANDO tenta enviar o quarto ENTÃO a API retorna HTTP 429 o  botão  "Enviar  ticket"  exibe  mensagem  de  erro  "Limite de 3 tickets/hora atingido. Tente em 42 min." Critério 6: Layout Mobile (coluna única) DADO QUE o usuário acessa /suporte em um dispositivo de 375px QUANDO a página carrega ENTÃO o FAQ e o formulário são exibidos em coluna única (FAQ primeiro, formulário abaixo) o layout não usa split lateral Critério 7: Link de contato de fallback DADO QUE o usuário visualiza o formulário de ticket em Desktop QUANDO vê o texto "Ou contate: secretaria@ufpr.br" QUANDO

clica no e-mail ENTÃO

o cliente de e-mail padrão abre com o destinatário pré-preenchido DIAGRAMAS DE SEQUÊNCIA F8.2-D01 - FAQ: carregamento dinâmico por perfil Escopo: happy path - carregamento inicial da página /suporte; FAQ renderizado com perguntas ordenadas por perfil do usuário Atores: Usuário autenticado (qualquer perfil), WebApp Pré-condições: JWT válido; tabela faq\_items populada pelo admin

<!-- image -->

Notas: Passo 4: a ordenação por perfil é configurável pelo admin (RN-12); o parâmetro ?perfil= espelha o claim  role  do  JWT  -  o  controller  resolve  o  valor  a  partir  do  token,  o  usuário  não  o  envia manualmente. FAQ é read-only para todos os perfis; gerenciamento dos itens fica fora do escopo F8 (admin via US-F7). O primeiro item é expandido por padrão (defaultOpen: true) no componente DS/Accordion - é uma decisão de UI, não uma flag da API.

F8.2-D02 - Enviar ticket com sucesso (POST + TX + outbox + protocolo)

Escopo:  happy  path  -  usuário  submete  ticket;  backend  cria  request  SUPORTE\_TECNICO  via workflow engine e insere evento outbox Atores: Usuário autenticado, WebApp Pré-condições: JWT válido; Zod client-side válido (campos preenchidos); RequestType=SUPORTE\_TECNICO configurado no workflow engine (RN-08)

<!-- image -->

Notas: Passos 5-8: transação atômica - falha em qualquer INSERT faz rollback completo; o outbox\_event garante que a notificação à secretaria será enviada mesmo em caso de falha pós-COMMIT. RequestType=SUPORTE\_TECNICO  reutiliza  o  workflow  engine  (RN-08,  fluxos  §9.2)  -  o  ticket entra na fila  de  triagem  da secretaria como qualquer outra solicitação. Alternativa MVP sem tipo configurado: INSERT direto em support\_thread. Dispatch do outbox (notificação secretaria): → transversal/10.1-outbox-notificacao.md  (não redesenhar aqui). Formulário  permanece  visível  após  sucesso;  apenas  limpo  (não  navega)  -  comportamento client-side, sem nova chamada de API.

F8.2-D03 - Rate limit 429 (Bucket4j - 3 tickets/hora)

Escopo:  erro  -  usuário  tenta  submeter  quarto  ticket  na  mesma  hora;  Bucket4j  rejeita  antes  de tocar o banco Atores: Usuário autenticado, WebApp Pré-condições: JWT válido; usuário já enviou 3 tickets na última hora

<!-- image -->

Notas: Passo 3: o Bucket4j intercepta antes de qualquer lógica de negócio - nem CreateTicketUseCase nem Postgres são tocados (RN-11). O header Retry-After (RFC 7807 + RFC 6585) acompanha o 429; o frontend lê retryAfterMinutes do body para exibir o countdown de X min. Rate limiting é por userId (não por IP) para evitar bloqueio de NAT compartilhado.

## 9 MODELAGEM DO BANCO DE DADOS

LINK MODELO CONCEITUAL

FIGURA xx - Modelo conceitual do banco de dados transacional.

<!-- image -->

FIGURA xx - Modelo conceitual modulos nucleo e iam.

<!-- image -->

FIGURA xx - Modelo conceitual modulos academico e solicitações.

<!-- image -->

FIGURA xx - Modelo conceitual modulos formativas, estágio e tcc.

<!-- image -->

FIGURA xx - Modelo conceitual modulos comunicação, presença e certificados

<!-- image -->

link modelo lógico https:/ /dbdocs.io/zuriann7/ModeloLogico FIGURA xx - Modelo lógico do banco de dados transacional link do modelo físico https:/ /dbdocs.io/zuriann7/SecretariaOnline2

<!-- image -->

FIGURA xx - Modelo físico do banco de dados transacional