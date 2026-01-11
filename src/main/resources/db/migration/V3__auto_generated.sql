ALTER TABLE recipes ADD CONSTRAINT recipes_name_author_id_unique UNIQUE ("name", author_id);
ALTER TABLE recipe_comments ADD CONSTRAINT recipe_comments_recipe_id_author_id_unique UNIQUE (recipe_id, author_id);
