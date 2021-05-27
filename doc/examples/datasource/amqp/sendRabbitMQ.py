import pika
import sys
import argparse

def parseArguments():

    #Create argument parser
    parser = argparse.ArgumentParser()

    parser.add_argument("exchangeType", help="Broker exchange type [none] is default exchange type", type=str)
    parser.add_argument("message", help="Send a message", type=str)
    parser.add_argument("-host", "--host", help="IP address of Broker server", type=str, default="localhost")
    parser.add_argument("-port", "--port", help="Port of the broker", type=int, default=5672)
    parser.add_argument("-u", "--username", help="Username", type=str, default="guest")
    parser.add_argument("-p", "--password", help="User password", type=str, default="guest")
    parser.add_argument("-vh", "--virtualHost", help="RabbitMQ Virtual Host", type=str, default="/")
    parser.add_argument("-e", "--exchangeName", help="Exchange name", type=str, default="")
    parser.add_argument("-cd", "--channelDurable", help="Channel Durability", type=bool, default=True)
    parser.add_argument("-k", "--routingKey", help="Routing Key", type=str, default="")
    parser.add_argument("-q", "--queueName", help="Exchange name", type=str, default="")

    parser.add_argument("--version", action="version", version='%(prog)s - Version 1.0')

    args = parser.parse_args()

    return args

if __name__ == '__main__':

    def callback(ch, method, properties, body):
        print(" [x] %r:%r" % (method.routing_key, body))

    args = parseArguments()
    
    ## Create connection
    credentials = pika.PlainCredentials(args.username, args.password)
    conn = pika.BlockingConnection(pika.ConnectionParameters(args.host, args.port, args.virtualHost, credentials))
    channel = conn.channel()
    
    if not args.exchangeType == "none":
        channel.exchange_declare(exchange=args.exchangeName, exchange_type=args.exchangeType, durable=args.channelDurable)
        channel.basic_publish(exchange=args.exchangeName, routing_key=args.routingKey, body=args.message)
        print(" [X] Sent %r:%r" % (args.routingKey, args.message))
        

    if args.exchangeType == "none":

        channel.queue_declare(queue=args.queueName)
        channel.basic_publish(exchange='', routing_key=args.queueName, body=args.message)
        print(" [X] Sent %r" % (args.message))
    
    conn.close()
