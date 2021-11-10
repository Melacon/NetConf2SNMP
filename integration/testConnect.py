from ncclient import manager
import sys


args = sys.argv
args.pop(0)

if len(args) <2:
    print("please start with testConnect.py ip port")
    exit(1)

host = args.pop(0)
port = args.pop(0)



with manager.connect(host=host,
                 port=int(port),
                 username='admin',
                 password='admin', hostkey_verify=False) as mgr:
    
    print("got connect")
    for c in mgr.server_capabilities:
            print(c.strip())
    print("get config for running")
    c = mgr.get_config(source='running', filter=('xpath', '/data/network-element')).data_xml
    print(c)
    print("done")