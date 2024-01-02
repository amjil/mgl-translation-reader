-- Place your queries here. Docs available https://www.hugsql.org/

-- :name query-articles :? :*
-- :doc query articles
select 
    a.id,
    a.original_lang,
    substring(a.content, 200) as content,
    a.original_url,
    a.created_at,
    b.email
from articles a
    left join users b on a.user_id = b.id
order by a.created_at desc
limit :limit
offset :offset

-- :name query-article-sentences :? :*
-- :doc query articles sentences
select 
    a.article_id,
    a.user_id,
    a.serial_number,
    a.lang,
    a.content,
    b.email,
    a.created_at
from article_sentences a
    left join users on a.user_id = b.id
order by a.serial_number