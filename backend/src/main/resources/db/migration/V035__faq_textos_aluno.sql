-- Textos do FAQ em linguagem para o aluno (F8.2).

UPDATE faq_item
SET
    pergunta = 'Como redefinir minha senha?',
    resposta = $txt$Você pode trocar a senha de dois jeitos:

1) Se já estiver dentro do sistema
• No menu lateral, abra Configurações → Segurança.
• Em “Trocar senha”, informe a senha atual, digite a nova senha (ela precisa ser forte) e confirme.
• Clique em “Salvar senha”. A troca vale na hora.

2) Se não conseguir entrar (esqueceu a senha)
• Na tela de login, use a opção de recuperar senha.
• Informe o e-mail institucional (@ufpr.br), o e-mail pessoal cadastrado ou o GRR.
• Você receberá um link para criar uma nova senha. Esse link vale por 24 horas.
• Abra o link, defina a nova senha e volte ao login.

Dica: se o primeiro acesso ainda pedir troca de senha, siga as instruções da própria tela até concluir.$txt$,
    updated_at = NOW()
WHERE id = '018f0000-0000-7000-8000-000000000361';

UPDATE faq_item
SET
    pergunta = 'Qual é o prazo de deliberação?',
    resposta = $txt$O prazo depende do tipo da solicitação. Conta a partir do momento em que o pedido é aberto e aparece na tela Solicitações, na coluna de prazo.

Prazos atuais no sistema:
• Declaração simples — 15 dias
• Autorização de uso de imagem — 15 dias
• Suporte técnico — 5 dias

Outros tipos que a secretaria publicar no futuro terão o próprio prazo. Se a data passar sem resposta, a solicitação aparece em atraso (SLA) para a equipe que delibera. Você continua acompanhando o andamento em Solicitações.$txt$,
    updated_at = NOW()
WHERE id = '018f0000-0000-7000-8000-000000000362';
