import pika
import sys
import argparse
from time import gmtime, strftime

def parseArguments():

    #Create argument parser
    parser = argparse.ArgumentParser()

    parser.add_argument("exchangeType", help="Broker exchange type [none] is default exchange type", type=str)
    parser.add_argument("-host", "--host", help="IP address of Broker server", type=str, default="localhost")
    parser.add_argument("-port", "--port", help="Port of the broker", type=int, default=5672)
    parser.add_argument("-u", "--username", help="Username", type=str, default="guest")
    parser.add_argument("-p", "--password", help="User password", type=str, default="guest")
    parser.add_argument("-vh", "--virtualHost", help="RabbitMQ Virtual Host", type=str, default="/")
    parser.add_argument("-e", "--exchangeName", help="Exchange name", type=str, default="")
    parser.add_argument("-cd", "--channelDurable", help="Channel Durability", type=bool, default=True)
    parser.add_argument("-k", "--routingKey", help="Routing Key", type=str, default="")
    parser.add_argument("-q", "--queueName", help="Queue name", type=str, default="")
    parser.add_argument("-s", "--saveLogs", help="Save logs to file", type=bool, default=False)
    parser.add_argument("-d", "--saveDirectory", help="Save log directory", type=str, default="")

    parser.add_argument("--version", action="version", version='%(prog)s - Version 1.0')

    args = parser.parse_args()

    return args

def convertRoutingKey(keyList):
    
    routing_key = ""
    routing_keys = keyList.split(".")
    for key in routing_keys:
        if key == '*':
            key = "any"
        if key == '#':
            key = "all"
        routing_key = routing_key + key + "-"
    return routing_key[:-1]

if __name__ == '__main__':

    first_run = True
    def callback(ch, method, properties, body):

        if(args.saveLogs):
            global f
            global first_run
            if first_run:
                f.write(body + "]")
                first_run = False
            else:
                f.seek(-1,2)
                f.truncate()
                f.write("," + body + "]")
        else:
            print(" [x] %r:%r" % (method.routing_key, body))

    args = parseArguments()
    
    ## Create connection
    credentials = pika.PlainCredentials(args.username, args.password)
    conn = pika.BlockingConnection(pika.ConnectionParameters(args.host, args.port, args.virtualHost, credentials))

    channel = conn.channel()
    exType = args.exchangeType
    if args.exchangeType == "none":
        exType = ''

    if not args.exchangeType == "none":
        result = channel.queue_declare(queue=args.queueName, durable=args.channelDurable, exclusive=True, auto_delete=False)
        queue_name = result.method.queue

        channel.queue_bind(exchange=args.exchangeName, queue=queue_name, routing_key=args.routingKey)
        channel.basic_consume(queue_name, callback)

    if args.exchangeType == "none":
        channel.queue_bind(exchange=args.exchangeName, queue=args.queueName, routing_key=args.routingKey)
        channel.basic_consume(queue_name, callback)

    date = strftime("%Y-%m-%d__%H-%M", gmtime())    
    print(" [*] " + date + " Waiting for logs from " + args.exchangeName + "_" + args.routingKey)
    
    
    if(args.saveLogs):
        file_name = "LOG_" + date + "-" + args.virtualHost + "_" + args.exchangeName + "_" + convertRoutingKey(args.routingKey) + ".json"
        print("Saving to file " + args.saveDirectory + file_name)
        f = open(args.saveDirectory + file_name, "a+")
        f.write("[")

    channel.start_consuming()
    
