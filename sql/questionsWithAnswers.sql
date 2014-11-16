select 	questions.id as questionId, 
		answers.id as answerId,
		answers.OwnerUserId
from 
(select * from posts 
where PostTypeId = 1) as questions
inner join 
(select * from posts  
where PostTypeId = 2) as answers
on questions.id = answers.ParentId;