select distinct  sa.alfresco_node_id,sa.file_name , sa.script_attachment_id, st.title
from script_attachment sa , script_title st 
where sa.script_id = st.script_id and st.aka_ind ='N' and sa.alfresco_node_id is not null
;