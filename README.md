# Reinos Perna

**Reinos Perna** é um plugin para Minecraft que simula um efeito de "perna quebrada" no jogador, com penalidades de lentidão e outros efeitos. Os jogadores podem se curar usando um comando ou item específico.

Plugin criado para o servidor **Reinos Survival**

## Funcionalidades

- **Quebra de Perna**: Quando um jogador sofre uma queda alta, ele quebra a perna e recebe efeitos de lentidão e salto reduzido.
- **Comando de Cura**: Administradores podem curar jogadores com a perna quebrada usando o comando `/curarperna <jogador>`.
- **Prevenção de Cura com Leite**: Jogadores com a perna quebrada não podem curar-se com leite. Uma mensagem será exibida quando tentarem.
- **Partículas de Sangue**: Partículas de sangue são geradas quando o jogador quebra a perna, aumentando a imersão.
- **Comandos**:
  - `/curarperna <jogador>`: Cura a perna de um jogador específico.

## Configuração

O plugin salva as informações de lesões dos jogadores no arquivo de configuração `config.yml`, onde é possível configurar o status da perna quebrada de cada jogador.

Exemplo de configuração:
```yaml
brokenLeg:
  JogadorExemplo: true
