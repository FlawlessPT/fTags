# TODO fTags

- Tag Mito (jogador com mais kills).
- Tag Gladiador (último vencedor do evento).
- Tags no tab list.
- Menu gráfico para gerir tags.
- Cache de tags no chat (evitar consulta por mensagem).
- `/tags add|remove <jogador> magnata` é sobrescrito pelo próximo refresh (até `refresh-seconds`).
- `Bukkit.getOfflinePlayer(String)` no `TagsCommand` corre na thread principal e em 1.8 pode fazer lookup bloqueante para nomes desconhecidos.
- Validar no `TagsConfig` que `auto` implica `unique` (senão `applyRichest` acumula detentores).
- Índice em `tag_id` (migração V002) se o bloqueio de linhas pesar.
- fChat sem timeout se o fTags for lento (cache de tags).
