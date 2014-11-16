select users.Id, users.DisplayName, users.Reputation, count(posts.id)
from posts, users
where posts.OwnerUserId = users.id and posts.PostTypeId = 2
group by posts.OwnerUserId
Order by count(posts.id) Desc