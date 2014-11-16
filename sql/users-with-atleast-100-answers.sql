select userId
from (
select users.Id as userId, count(posts.id) as postsCount
from posts, users
where posts.OwnerUserId = users.id and posts.PostTypeId = 2 
group by posts.OwnerUserId ) as UsersWithAnswers
where postsCount > 100;