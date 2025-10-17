import numpy as np

# Input Tensor (3 channels, 5x5 each)
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
    ],
    [  # Channel 2
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1],
        [1, 1, 1, 1, 1]
    ]
])

# Pointwise filters (4 filters, each with 3 input channel weights for 1x1)
pointwise_filters = np.array([
    [  # Filter 0
        [1, -1,1]  # One weight per input channel
    ],
    [  # Filter 1
        [0, 2,1]
    ],
    [  # Filter 2
        [-1, 0,1]
    ],
    [  # Filter 3
        [-2, 1,1]
    ]
])  # shape: (4 filters, 1x1, 3 input channels)

in_channels, H, W = input_tensor.shape
num_filters = pointwise_filters.shape[0]
print(f"Input Tensor Shape: {input_tensor.shape}")
print(f"Pointwise Filters Shape: {pointwise_filters.shape}")
output = np.zeros((num_filters, H, W), dtype=int)

for f in range(num_filters):
    print(f"\n=== Filter {f} ===")
    for i in range(H):
        for j in range(W):
            acc = 0
            print(f"\nOutput[{f}][{i}][{j}] computation:")
            for c in range(in_channels):
                inp_val = input_tensor[c, i, j]
                filt_val = pointwise_filters[f, 0, c]
                prod = inp_val * filt_val
                acc += prod
                print(f"Input[{c}][{i}][{j}] × Filter[{f}][{0}][{c}] = {inp_val}×{filt_val} = {prod}")
            output[f, i, j] = acc
            print(f"=> Output[{f}][{i}][{j}] = {acc}")


print("\nFinal Pointwise Convolution Output:")
print(output)
