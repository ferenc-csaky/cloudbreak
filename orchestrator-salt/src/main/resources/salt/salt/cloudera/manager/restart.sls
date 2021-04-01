/var/lib/cloudera-scm-agent/agent-cert:
  file.directory:
    - user: root
    - group: root
    - mode: 755

stop-cloudera-scm-server:
  service.dead:
    - name: cloudera-scm-server
{% if 'cm_primary' in grains.get('roles', []) %}
start-cloudera-scm-server:
  service.running:
    - name: cloudera-scm-server
{% endif %}
