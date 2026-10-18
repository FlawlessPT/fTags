# TODO fTags

- Tag Mito (jogador com mais kills).
- Tag Gladiador (último vencedor do evento).
- Tags no tab list.
- Cache de tags no chat (evitar consulta por mensagem).
- `/tags add|remove <jogador> magnata` é sobrescrito pelo próximo refresh (até `refresh-seconds`).
- `Bukkit.getOfflinePlayer(String)` no `TagsCommand` corre na thread principal e em 1.8 pode fazer lookup bloqueante para nomes desconhecidos.
- Validar no `TagsConfig` que `auto` implica `unique` (senão `applyRichest` acumula detentores).
- Índice em `tag_id` (migração V002) se o bloqueio de linhas pesar.
- fChat sem timeout se o fTags for lento (cache de tags).
- O menu `/tags` mostra no máximo 9 tags (uma coluna cada), sem páginas.
- O tab não tem consumidor ainda: a preferência `visible_tab` só é guardada e exposta via `getVisibleTags(uuid, TagSurface.TAB)`.
- Toggle não atómico: cliques rápidos/duplo-clique num vidro podem perder uma alternância (ignorar cliques enquanto há um toggle em curso por vidro).
- Pista `/tags help` para admins quando o subcomando não existe.
