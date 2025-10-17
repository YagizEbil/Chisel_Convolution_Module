import numpy as np

# Input Tensor (shape: [channels=2, height=5, width=5])
input_tensor = np.array([
    [  # Channel 0
        [1, 2, 2, 2, 1],
        [3, 0, 3, 0, 3],
        [1, 1, 1, 1, 1],
        [1, 3, 3, 3, 1],
        [1, 1, 1, 1, 1]
    ],
    [  # Channel 1
        [0, 1, 0, 2, 0],
        [1, 1, 0, 1, 1],
        [4, 1, 0, 1, 3],
        [4, 1, 0, 1, 2],
        [0, 1, 0, 2, 0]
    ]
])

# Filters (shape: [num_filters=3, channels=2, height=3, width=3])
filters = np.array([
    [  # Filter 0
        [  # Channel 0
            [-1, 0, 1],
            [ 0, -1, 1],
            [ 0,  1, -1]
        ],
        [  # Channel 1
            [-1, -1, 1],
            [-1,  1, 0],
            [ 1,  0, 0]
        ]
    ],
    [  # Filter 1
        [  # Channel 0
            [-1, 0, 1],
            [ 0, -1, 1],
            [ 0,  1, -1]
        ],
        [  # Channel 1
            [-1, -1, 1],
            [-1,  1, 1],
            [ 1,  1, 1]
        ]
    ],
    [  # Filter 2
        [  # Channel 0
            [-1, -1, 1],
            [ -1,  1, 1],
            [ 1,  1, 1]
        ],
        [  # Channel 1
            [-1, -1, 1],
            [-1,  1, 0],
            [ 1,  0, 0]
        ]
    ]
])

# Output dimensions
output_h = input_tensor.shape[1] - 3 + 1
output_w = input_tensor.shape[2] - 3 + 1
num_filters = filters.shape[0]

output = np.zeros((num_filters, output_h, output_w), dtype=int)

for f in range(num_filters):
    for i in range(output_h):
        for j in range(output_w):
            acc = 0
            print(f"\nFilter #{f} at position ({i},{j}):")
            for c in range(2):  # number of channels
                for m in range(3):
                    for n in range(3):
                        inp_val = input_tensor[c, i + m, j + n]
                        filt_val = filters[f, c, m, n]
                        prod = inp_val * filt_val
                        acc += prod
                        print(f"Input[{c}][{i+m}][{j+n}] × Filter[{f}][{c}][{m}][{n}] = {inp_val}×{filt_val} = {prod}")
            output[f, i, j] = acc
            print(f"=> Output[{f}][{i}][{j}] = {acc}")

print("\nFinal Output Tensor:")
print(output)
