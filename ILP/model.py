from pyomo.environ import (
    minimize,
    AbstractModel, SolverFactory,
    Constraint, Var, Objective, Set,
    NonNegativeIntegers, Binary, TerminationCondition
)


class NonOptimalException(Exception):
    pass


def add_params(model):
    """This function defines all parameters to the model"""
    # vertices
    # U would be 0...m-1
    # V would be 0...n-1
    model.U = Set()
    model.V = Set()
    # edges
    model.E = Set(within=model.U * model.V)


def add_vars(model):
    """This function defines all the variables of the model"""
    # If edge e1 and e2 cross and e1 goes "under" e2 then x[e1, e2] = 1.
    # This means that e1 would take the gap.
    # The variable is binary (only 0 and 1 values)
    model.x = Var(model.E, model.E, domain=Binary)
    # g is an upper bound on the number of gaps
    # We want to minimize g to get as few gaps as possible
    # g takes integer values
    model.g = Var(domain=NonNegativeIntegers)
    # permutation of U
    # pu[u1, u2] == 1 means that u1 comes before u2 in the order
    model.pu = Var(model.U, model.U, domain=Binary)
    # permutation of V
    # pv[v1, v2] == 1 means that v1 comes before v2 in the order
    model.pv = Var(model.V, model.V, domain=Binary)


###########################################################
# constraint functions

def gap_upper_bound(model, es, et):
    # This makes sure that g bounds the number of gaps as for a given edge e
    # the number of gaps it takes is sum(x[e, f], f in E)
    return model.g >= sum(model.x[(es, et), f] for f in model.E)


def transitive_u(model, u1, u2, u3):
    # Transitive property for the vertex ordering
    return (0, model.pu[u1, u2] + model.pu[u2, u3] - model.pu[u1, u3], 1)


def total_order_u(model, u1, u2):
    # Makes sure that the order is total, that is for given u1 != u2 either
    # u1 < u2 or u2 < u1
    if u1 == u2:
        return Constraint.Feasible
    return model.pu[u1, u2] + model.pu[u2, u1] == 1


def transitive_v(model, v1, v2, v3):
    # Transitive property for the vertex ordering
    return (0, model.pv[v1, v2] + model.pv[v2, v3] - model.pv[v1, v3], 1)


def total_order_v(model, v1, v2):
    # Makes sure that the order is total, that is for given u1 != u2 either
    # u1 < u2 or u2 < u1
    if v1 == v2:
        return Constraint.Feasible
    return model.pv[v1, v2] + model.pv[v2, v1] == 1


def cross_implies_x(model, es, et, fs, ft):
    # if e and f cross then e goes under f or the other way around
    # if pu[e[0], f[0]] == 1 and pv[f[1], e[1]] == 1 ==> x[e, f] + x[f, e] == 1
    # The logic here is that the left hand side can only take values -1, 0 and
    # 1 and it's 1 exactly when both variable terms are 1.
    # So this is the only case in where the constraint has some effect (because
    # the right hand side is always positive).
    # In this case it forces the right hand side to be strictly positive so
    # one of the x's has to be one (which is what we wanted)
    return (
        model.pu[es, fs] + model.pv[ft, et] - 1 <=
        model.x[(es, et), (fs, ft)] + model.x[(fs, ft), (es, et)]
    )

###########################################################


def add_constraints(model):
    """Function that adds all the previous constraints to the model"""
    model.gap_upper_bound_rule = Constraint(model.E, rule=gap_upper_bound)
    model.transitive_u_rule = Constraint(
        model.U, model.U, model.U, rule=transitive_u
    )
    model.total_order_u_rule = Constraint(model.U, model.U, rule=total_order_u)
    model.transitive_v_rule = Constraint(
        model.V, model.V, model.V, rule=transitive_v
    )
    model.total_order_v_rule = Constraint(model.V, model.V, rule=total_order_v)
    model.cross_implies_x_rule = Constraint(
        model.E, model.E, rule=cross_implies_x
    )


def objective(model):
    """Compute cost"""
    return (
        1000 * model.g + sum(model.x[e, f] for e in model.E for f in model.E)
    )


def get_model():
    """This function prepares the abstract model"""
    model = AbstractModel()
    add_params(model)
    add_vars(model)
    add_constraints(model)
    model.OBJ = Objective(rule=objective, sense=minimize)
    return model


def optimize(data, timeout, debug, solver_name):
    """Create an instance of the abstract model and optimize it"""
    model = get_model()
    instance = model.create_instance(data)
    # replace cbc with gurobi to use gurobi
    solver = SolverFactory(solver_name)
    if solver_name == 'gurobi':
        solver.options['TimeLimit'] = timeout
    elif solver_name == 'cbc':
        solver.options['sec'] = timeout
    opt = solver.solve(instance, tee=debug)
    if opt.solver.termination_condition != TerminationCondition.optimal:
        raise NonOptimalException
    return instance
