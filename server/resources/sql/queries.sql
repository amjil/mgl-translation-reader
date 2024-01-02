-- Place your queries here. Docs available https://www.hugsql.org/

-- :name query-articles :? :*
-- :doc query articles
select 
    a.*, b.email
from articles a
    left join users where a.user_id = b.id
order by a.created_at desc
limit :limit
offset :offset

-- :name query-article-sentences :? :*
-- :doc query articles sentences
select 
    a.*, b.email
from article_sentences a
    left join users on a.user_id = b.id
order by a.serial_number