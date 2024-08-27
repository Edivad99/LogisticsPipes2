with open("pipe_transport_basic.obj", "r") as f:
    lines = f.readlines()
    for i in range(len(lines)):
        if lines[i].startswith("v "):
            line = lines[i].split(" ")
            line[1] = "%.6f" % (float(line[1]) * 0.01)
            line[2] = "%.6f" % (float(line[2]) * 0.01)
            line[3] = "%.6f" % (float(line[3]) * 0.01)
            lines[i] = " ".join(line) + "\n"

    with open("pipe_transport_basic_scaled.obj", "w") as f:
        f.writelines(lines)
