#!/bin/bash

# Inicia o Kafka Connect original em background
/docker-entrypoint.sh start &

# 2. Aguarda 60 segundos conforme solicitado para estabilização dos serviços
# Isso dá tempo para o SQL Server terminar o setup e o Debezium carregar os plugins
echo "Iniciando espera de 60 segundos para estabilização do SQL Server e Debezium..."
sleep 60

# 3. Loop adicional de segurança: verifica se a porta 8083 do Debezium está respondendo
echo "Verificando disponibilidade da porta 8083..."
until curl -s http://localhost:8083/connectors > /dev/null; do
  echo "Debezium ainda não está pronto. Aguardando mais 5 segundos..."
  sleep 5
done

echo "Serviços estabilizados. Registrando o conector SQL Server..."

# 4. Registra o conector usando o arquivo montado pelo volume
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" \
  http://localhost:8083/connectors/ -d @/kafka/connect-debezium.json

echo "Registro finalizado."

# 5. Mantém o container vivo acompanhando o processo em background
wait