src :: FromDevice
dst :: ToDevice

cl_incoming :: IPClassifier(ip dst net 141.85.228.0/26,-)

src -> cl_incoming[0] -> dst