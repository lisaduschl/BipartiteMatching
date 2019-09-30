import click
from pyomo.environ import value

from model import optimize, NonOptimalException


def get_data(m, n, e):
    """This function puts the data in pyomo format"""
    data = {
        None: {
            'U': {None: list(range(m))},
            'V': {None: list(range(n))},
            'E': {None: e},
        }
    }
    return data


def get_positions(m, p):
    d = [0 for _ in range(m)]
    for u2 in range(m):
        pos = int(sum(value(p[u1, u2]) for u1 in range(m)))
        d[pos] = u2
    return d


@click.command()
@click.option('--complete', is_flag=True, help='Use a complete graph')
@click.option(
    '--solver', type=click.Choice(['cbc', 'gurobi']),
    default='gurobi', help='Choose the ILP solver'
)
@click.option('--output', is_flag=True, help='Show solver output')
@click.option(
    '--timeout', default=900, type=int, help='Timeout for the ILP solver'
)
def main(complete, timeout, output, solver):
    if complete:
        m, n = list(map(int, input('Enter m n: ').split()))
        r = m * n
        e = [(i, j) for i in range(m) for j in range(n)]
    else:
        m, n, r = list(map(int, input('Enter m n r: ').split()))
        e = []
        print('Enter edges, one by line:')
        for _ in range(r):
            e.append(tuple(map(int, input().split())))
    # solve problem
    try:
        instance = optimize(get_data(m, n, e), timeout, output, solver)
        optimum = int(value(instance.OBJ)) // 1000
        crossings = int(value(instance.OBJ)) % 1000
        print(f'Graph is {optimum}-gap planar with {crossings} crossings')
        print('Order for the top vertices')
        print(' '.join(map(str, get_positions(m, instance.pu))))
        print('Order for the bottom vertices')
        print(' '.join(map(str, get_positions(n, instance.pv))))
        print('Edge gaps')
        for x in e:
            print(
                x, 'takes gap for crossing with',
                ' '.join([
                        str(y) for y in e
                        if int(value(instance.x[x, y]) + 0.5) == 1
                    ])
            )
    except NonOptimalException:
        print("failed")


if __name__ == '__main__':
    main()
