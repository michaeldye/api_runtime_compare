n_lim = 100
n = 1

request = function()
  path = "/api/fib/" .. n
  if n == n_lim then
    n = 1
  else
    n = n + 1
  end
  return wrk.format("GET", path)
end
