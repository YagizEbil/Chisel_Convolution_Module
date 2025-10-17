import numpy as np

# Input Tensor (2 channels, 5x5 each)
input_tensor = np.array([
    [  # Channel 0
        [1, 2, 1, 0, 2],
        [0, 1, 2, 2, 1],
        [3, 0, 1, 1, 1],
        [1, 2, 0, 1, 0],
        [0, 1, 2, 1, 2]
    ],
    [  # Channel 1
        [2, 0, 1, 2, 0],
        [1, 1, 0, 1, 1],
        [0, 2, 2, 0, 1],
        [1, 0, 1, 1, 2],
        [2, 1, 0, 2, 1]
    ]
])

# Depthwise filters: one filter per input channel (2 filters of size 3x3)
depthwise_filters = np.array([
    [  # Filter for channel 0
        [-1,  0,  1],
        [ 0, -1,  1],
        [ 1,  0, -1]
    ],
    [  # Filter for channel 1
        [ 1,  0, -1],
        [ 1, -1,  0],
        [-1, 1,  0]
    ]
])

# Output Tensor (same number of channels, output size = 3x3)
output_h = input_tensor.shape[1] - 3 + 1
output_w = input_tensor.shape[2] - 3 + 1
num_channels = input_tensor.shape[0]

output = np.zeros((num_channels, output_h, output_w), dtype=int)

for c in range(num_channels):
    for i in range(output_h):
        for j in range(output_w):
            acc = 0
            print(f"\nChannel {c}, Output({i},{j}):")
            for m in range(3):
                for n in range(3):
                    inp_val = input_tensor[c, i + m, j + n]
                    filt_val = depthwise_filters[c, m, n]
                    prod = inp_val * filt_val
                    acc += prod
                    print(f"Input[{c}][{i+m}][{j+n}] × Filter[{c}][{m}][{n}] = {inp_val}×{filt_val} = {prod}")
            output[c, i, j] = acc
            print(f"=> Output[{c}][{i}][{j}] = {acc}")

print("\nFinal Depthwise Convolution Output:")
print(output)
