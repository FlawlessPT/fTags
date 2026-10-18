-- visible passa a ser a visibilidade no chat; visible_tab é a da lista de jogadores
ALTER TABLE ftags_owned ADD COLUMN visible_tab BOOLEAN NOT NULL DEFAULT TRUE;
